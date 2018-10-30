package com.nicfeanny.flickrbrowser;


/*
*   This class exists to hold the data of a single photo.
* */
public class Photo {
    /*
     * These attributes represent the fields within the JSON Object
     * that we will be reading from and pulling.
     * */
    private String title;
    private String author;
    private String authorID;
    private String link;
    private String tags;
    private String image;


    /*
     * We will not be storing the data for the photo
     * This is because storage on a mobile device is limited
     * We will be using a process which downloads the photo just
     *   before will need it
     * !!!!!We won't be downloading a load of images and trying to
     *           store them in other words.
     *
     * Space restriction is super important for mobile devices.
     * */



    //CMD N for shortcut on the Mac to pre create the constructor with the params.
    public Photo(String title, String author, String authorID, String link, String tags, String image) {
        this.title = title;
        this.author = author;
        this.authorID = authorID;
        this.link = link;
        this.tags = tags;
        this.image = image;
    }

    /*
    * same command as the constructor on Mac (CMD N)
    * */
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorID() {
        return authorID;
    }

    public String getLink() {
        return link;
    }

    public String getTags() {
        return tags;
    }

    public String getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", authorID='" + authorID + '\'' +
                ", link='" + link + '\'' +
                ", tags='" + tags + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
