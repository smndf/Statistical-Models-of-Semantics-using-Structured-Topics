package images;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;

public class FlickrSearch {

	public final static String apikey = "4c0be53e8aaa99c47ce8b1e44ac4eebf";
	public final static String secret = "fdba48efca7701b0";
	
	public static String search(String word){

		Flickr flickr = new Flickr(apikey, secret, new REST());

	    SearchParameters searchParameters = new SearchParameters();
	    searchParameters.setText(word);
	    searchParameters.setSort(6);
	    try {
			PhotoList<Photo> list = flickr.getPhotosInterface().search(searchParameters, 1, 1);
			for (Photo photo : list){
				String url = photo.getSmall320Url();
				System.out.println("url flickr = " + url);
				return url;
			}
		} catch (FlickrException e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	
	
}
