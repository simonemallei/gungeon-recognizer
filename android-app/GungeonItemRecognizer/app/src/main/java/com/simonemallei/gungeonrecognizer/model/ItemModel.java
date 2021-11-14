package com.simonemallei.gungeonrecognizer.model;

import android.graphics.drawable.Drawable;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.model.ItemModel
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * ItemModel class containing the items and guns attributes.
 */
public class ItemModel {

    /**
     * A String containing Item/Gun's Name.
     */
    public String title;
    /**
     * An integer containing Item/Gun's ID.
     */
    public int ID;
    /**
     * An integer containing Item/Gun's database ID.
     */
    public int dbID;
    /**
     * A String containing Item/Gun's quote.
     */
    public String quote;
    /**
     * A String containing Item/Gun's quality.
     */
    public String quality;
    /**
     * A String containing Item/Gun's description.
     */
    public String description;
    /**
     * A Drawable containing Item/Gun's image.
     */
    public Drawable image;
    /**
     * A String containing Item/Gun's type.
     */
    public String type;
    /**
     * A String containing Item/Gun's wiki link.
     */
    public String link;
    /**
     * A String containing Gun's DPS.
     */
    public String dps;
    /**
     * A String containing Gun's mag's size.
     */
    public String magSize;
    /**
     * A String containing Gun's ammo.
     */
    public String ammo;
    /**
     * A String containing Gun's damage.
     */
    public String damage;
    /**
     * A String containing Gun's fire rate.
     */
    public String fireRate;
    /**
     * A String containing Gun's reload.
     */
    public String reload;
    /**
     * A String containing Gun's shot speed.
     */
    public String shotSpeed;
    /**
     * A String containing Gun's range.
     */
    public String range;
    /**
     * A String containing Gun's force.
     */
    public String force;
    /**
     * A String containing Gun's spread.
     */
    public String spread;

    public ItemModel(String title, int ID, int dbID, String quote, String quality,
                     String description, Drawable image, String type, String link){
        this.title = title;
        this.ID = ID;
        this.dbID = dbID;
        this.quote = quote;
        this.quality = quality;
        this.description = description;
        this.image = image;
        this.type = type;
        this.link = link;
    }

    public ItemModel(String title, int ID, int dbID, String quote, String quality, String description,
                     Drawable image, String type, String link, String dps, String magSize,
                     String ammo, String damage, String fireRate, String reload, String shotSpeed,
                     String range, String force, String spread) {
        this.title = title;
        this.ID = ID;
        this.dbID = dbID;
        this.quote = quote;
        this.quality = quality;
        this.description = description;
        this.image = image;
        this.type = type;
        this.link = link;
        this.dps = dps;
        this.magSize = magSize;
        this.ammo = ammo;
        this.damage = damage;
        this.fireRate = fireRate;
        this.reload = reload;
        this.shotSpeed = shotSpeed;
        this.range = range;
        this.force = force;
        this.spread = spread;
    }
}
