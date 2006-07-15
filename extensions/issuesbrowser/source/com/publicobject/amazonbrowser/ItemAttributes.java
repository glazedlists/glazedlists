package com.publicobject.amazonbrowser;

import java.util.Date;

public class ItemAttributes {

    private AudienceRating audienceRating = null;
    private String director = null;
    private ListPrice listPrice = null;
    private Date releaseDate = null;
    private Date theatricalReleaseDate = null;
    private String title = null;

    /**
     * The audience rating of the movie or TV show.
     * The value returned will be one of {G, PG, PG-13, R, NC-17, Unrated}.
     */
    public AudienceRating getAudienceRating() { return audienceRating; }
    public void setAudienceRating(AudienceRating audienceRating) { this.audienceRating = audienceRating; }

    /**
     * The director of the movie or TV show.
     */
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    /**
     * The price of this Item.
     */
    public ListPrice getListPrice() { return listPrice; }
    public void setListPrice(ListPrice listPrice) { this.listPrice = listPrice; }

    /**
     * The Date the Item was released.
     */
    public Date getReleaseDate() { return releaseDate; }
    public void setReleaseDate(Date releaseDate) { this.releaseDate = releaseDate; }

    /**
     * The Date the Item was released to theaters.
     */
    public Date getTheatricalReleaseDate() { return theatricalReleaseDate; }
    public void setTheatricalReleaseDate(Date theatricalReleaseDate) { this.theatricalReleaseDate = theatricalReleaseDate; }

    /**
     * The title (name) of this Item.
     */
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}