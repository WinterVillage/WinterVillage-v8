package de.wintervillage.main;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import de.wintervillage.main.adventcalendar.AdventCalendar;
import de.wintervillage.main.economy.EconomyManager;
import de.wintervillage.main.economy.shop.ShopManager;
import de.wintervillage.main.event.EventManager;
import de.wintervillage.main.plot.PlotHandler;
import de.wintervillage.main.plot.database.PlotDatabase;
import de.wintervillage.main.specialitems.SpecialItems;

public class WinterVillageModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(PlotDatabase.class).in(Singleton.class);
        this.bind(PlotHandler.class).in(Singleton.class);
        this.bind(ShopManager.class).in(Singleton.class);
        this.bind(EconomyManager.class).in(Singleton.class);
        this.bind(SpecialItems.class).in(Singleton.class);
        this.bind(EventManager.class).in(Singleton.class);
        this.bind(AdventCalendar.class).in(Singleton.class);
    }
}
