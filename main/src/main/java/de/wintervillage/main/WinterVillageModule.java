package de.wintervillage.main;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import de.wintervillage.main.plot.PlotHandler;
import de.wintervillage.main.plot.database.PlotDatabase;

public class WinterVillageModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(PlotDatabase.class).in(Singleton.class);
        this.bind(PlotHandler.class).in(Singleton.class);
    }
}
