package de.wintervillage.main.database;

import com.mongodb.MongoTimeoutException;
import com.mongodb.internal.thread.InterruptionUtil;
import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SubscriberHelpers {

    /**
     * A Subscriber that stores the publishers results and provides a latch so can block on completion.
     *
     * @param <T> The publishers result type
     */
    public abstract static class ObservableSubscriber<T> implements Subscriber<T> {
        private final List<T> received;
        private final List<RuntimeException> errors;
        private final CountDownLatch latch;
        private volatile Subscription subscription;
        private volatile boolean completed;

        /**
         * Construct an instance
         */
        public ObservableSubscriber() {
            this.received = new ArrayList<>();
            this.errors = new ArrayList<>();
            this.latch = new CountDownLatch(1);
        }

        @Override
        public void onSubscribe(final Subscription subscription) {
            this.subscription = subscription;
        }

        @Override
        public void onNext(T t) {
            this.received.add(t);
        }

        @Override
        public void onError(final Throwable t) {
            if (t instanceof RuntimeException) this.errors.add((RuntimeException) t);
            else this.errors.add(new RuntimeException("Unexpected exception", t));
            this.onComplete();
        }

        @Override
        public void onComplete() {
            this.completed = true;
            this.latch.countDown();
        }

        /**
         * Gets the subscription
         *
         * @return the subscription
         */
        public Subscription getSubscription() {
            return this.subscription;
        }

        /**
         * Get received elements
         *
         * @return the list of received elements
         */
        public List<T> getReceived() {
            return this.received;
        }

        /**
         * Get error from subscription
         *
         * @return the error, which may be null
         */
        public RuntimeException getError() {
            if (!this.errors.isEmpty()) return this.errors.getFirst();
            return null;
        }

        /**
         * Get received elements.
         *
         * @return the list of receive elements
         */
        public List<T> get() {
            return this.await().getReceived();
        }

        /**
         * Get received elements.
         *
         * @param timeout how long to wait
         * @param unit    the time unit
         * @return the list of receive elements
         */
        public List<T> get(final long timeout, final TimeUnit unit) {
            return this.await(timeout, unit).getReceived();
        }


        /**
         * Get the first received element.
         *
         * @return the first received element
         */
        public T first() {
            List<T> received = await().getReceived();
            return !received.isEmpty() ? received.getFirst() : null;
        }

        /**
         * Await completion or error
         *
         * @return this
         */
        public ObservableSubscriber<T> await() {
            return this.await(60, TimeUnit.SECONDS);
        }

        /**
         * Await completion or error
         *
         * @param timeout how long to wait
         * @param unit    the time unit
         * @return this
         */
        public ObservableSubscriber<T> await(final long timeout, final TimeUnit unit) {
            this.subscription.request(Integer.MAX_VALUE);
            try {
                if (!this.latch.await(timeout, unit))
                    throw new MongoTimeoutException("Publisher onComplete timed out");
            } catch (InterruptedException e) {
                throw InterruptionUtil.interruptAndCreateMongoInterruptedException("Interrupted waiting for observeration", e);
            }
            if (!this.errors.isEmpty()) throw this.errors.getFirst();
            return this;
        }
    }

    /**
     * A Subscriber that immediately requests Integer.MAX_VALUE onSubscribe
     *
     * @param <T> The publishers result type
     */
    public static class OperationSubscriber<T> extends ObservableSubscriber<T> {

        @Override
        public void onSubscribe(final Subscription subscription) {
            super.onSubscribe(subscription);
            subscription.request(Integer.MAX_VALUE);
        }
    }

    /**
     * A Subscriber that prints a message including the received items on completion
     *
     * @param <T> The publishers result type
     */
    public static class PrintSubscriber<T> extends OperationSubscriber<T> {
        private final String message;

        /**
         * A Subscriber that outputs a message onComplete.
         *
         * @param message the message to output onComplete
         */
        public PrintSubscriber(final String message) {
            this.message = message;
        }

        @Override
        public void onComplete() {
            System.out.printf((this.message) + "%n", getReceived());
            super.onComplete();
        }
    }

    /**
     * A Subscriber that prints the json version of each document
     */
    public static class PrintDocumentSubscriber extends ConsumerSubscriber<Document> {
        /**
         * Construct a new instance
         */
        public PrintDocumentSubscriber() {
            super(t -> System.out.println(t.toJson()));
        }
    }

    /**
     * A Subscriber that prints the toString version of each element
     *
     * @param <T> the type of the element
     */
    public static class PrintToStringSubscriber<T> extends ConsumerSubscriber<T> {
        /**
         * Construct a new instance
         */
        public PrintToStringSubscriber() {
            super(System.out::println);
        }
    }

    /**
     * A Subscriber that processes a consumer for each element
     *
     * @param <T> the type of the element
     */
    public static class ConsumerSubscriber<T> extends OperationSubscriber<T> {
        private final Consumer<T> consumer;

        /**
         * Construct a new instance
         *
         * @param consumer the consumer
         */
        public ConsumerSubscriber(final Consumer<T> consumer) {
            this.consumer = consumer;
        }


        @Override
        public void onNext(final T document) {
            super.onNext(document);
            this.consumer.accept(document);
        }
    }

    private SubscriberHelpers() { }
}
