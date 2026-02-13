package eee.eie4108.eie4108hello;

public class CD {
  private String title;
  private String artist;
  
  public CD(String title, String artist) {
    this.title = title;
    this.artist = artist;
  }
  
  public CD() {}
  
  public String getArtist() {
    return artist;
  }
  
  public String getTitle() {
    return title;
  }
}
