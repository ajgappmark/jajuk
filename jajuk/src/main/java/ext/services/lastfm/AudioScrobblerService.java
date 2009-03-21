/**
 * aTunes 1.6.6
 * Copyright (C) 2006-2007 Alex Aranda (fleax) alex@atunes.org
 *
 * http://www.atunes.org
 * http://sourceforge.net/projects/atunes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package ext.services.lastfm;

import ext.XMLUtils;
import ext.services.network.NetworkUtils;
import ext.services.network.Proxy;
import ext.services.xml.XMLBuilder;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jajuk.util.DownloadManager;
import org.jajuk.util.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class AudioScrobblerService {

  private static final String ARTIST_WILDCARD = "(%ARTIST%)";

  private static final String ALBUM_WILDCARD = "(%ALBUM%)";

  private static final String ALBUM_INFO_URL = "http://ws.audioscrobbler.com/1.0/album/"
      + ARTIST_WILDCARD + '/' + ALBUM_WILDCARD + "/info.xml";

  private static final String ALBUM_LIST_URL = "http://ws.audioscrobbler.com/1.0/artist/"
      + ARTIST_WILDCARD + "/topalbums.xml";

  private static final String SIMILAR_ARTIST_URL = "http://ws.audioscrobbler.com/1.0/artist/"
      + ARTIST_WILDCARD + "/similar.xml";

  private static final String ARTIST_TAG_URL = "http://ws.audioscrobbler.com/1.0/artist/"
      + ARTIST_WILDCARD + "/toptags.xml";

  private static final String NO_COVER_URL = "/depth/catalogue/noimage/cover_large.gif";

  private static final boolean SHOW_ALBUMS_WITHOUT_COVER = false;

  private final Proxy proxy;

  private static AudioScrobblerService self;

  /**
   * 
   * @return AudioScrobblerService singleton, note that this method is
   *         synchronized to make sure several views will not require the
   *         singleton at the same time (to avoid being banned from last.FM)
   */
  public static synchronized AudioScrobblerService getInstance() {
    // Wait a least one second to avoid being banned from LastFM
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
      Log.error(e);
    }
    if (self == null) {
      self = new AudioScrobblerService(DownloadManager.getProxy());
    }
    return self;
  }

  private AudioScrobblerService(Proxy proxy) {
    this.proxy = proxy;
  }

  public AudioScrobblerAlbum getAlbum(String artist, String album) {
    try {
      // build url
      String urlString = ALBUM_INFO_URL.replace(ARTIST_WILDCARD, NetworkUtils.encodeString(artist))
          .replace(ALBUM_WILDCARD, NetworkUtils.encodeString(album));
      // read xml
      Document xml = XMLBuilder.getXMLDocument(NetworkUtils.readURL(NetworkUtils.getConnection(
          urlString, proxy)));
      return AudioScrobblerAlbum.getAlbum(xml);
    } catch (IOException e) {
      Log.debug("No info found for artist " + artist + " album " + album);
    }
    return null;
  }

  public List<AudioScrobblerAlbum> getAlbumList(String artist) {
    try {
      // build url
      String urlString = ALBUM_LIST_URL.replace(ARTIST_WILDCARD, NetworkUtils.encodeString(artist));
      // read xml
      Document xml = XMLBuilder.getXMLDocument(NetworkUtils.readURL(NetworkUtils.getConnection(
          urlString, proxy)));
      List<AudioScrobblerAlbum> albums = AudioScrobblerAlbum.getAlbumList(xml);
      if (SHOW_ALBUMS_WITHOUT_COVER) {
        return albums;
      }

      List<AudioScrobblerAlbum> result = new ArrayList<AudioScrobblerAlbum>();
      for (AudioScrobblerAlbum a : albums) {
        if (!a.getSmallCoverURL().endsWith(NO_COVER_URL)) {
          result.add(a);
        }
      }
      return result;
    } catch (IOException e) {
      Log.debug("No info found for artist " + artist);
    }
    return null;
  }

  public AudioScrobblerSimilarArtists getSimilarArtists(String artist) {
    try {
      // build url
      String urlString = SIMILAR_ARTIST_URL.replace(ARTIST_WILDCARD, NetworkUtils
          .encodeString(artist));
      // read xml
      Document xml = XMLBuilder.getXMLDocument(NetworkUtils.readURL(NetworkUtils.getConnection(
          urlString, proxy)));
      return AudioScrobblerSimilarArtists.getSimilarArtists(xml);
    } catch (IOException e) {
      Log.debug("No info found for similar artists to artist " + artist);
    }
    return null;
  }

  public String getArtistTopTag(String artist) {
    try {
      // build url
      String urlString = ARTIST_TAG_URL.replace(ARTIST_WILDCARD, NetworkUtils.encodeString(artist));
      // read xml
      Document xml = XMLBuilder.getXMLDocument(NetworkUtils.readURL(NetworkUtils.getConnection(
          urlString, proxy)));
      return getTopTag(xml);
    } catch (IOException e) {
      Log.debug("No tag found for artist " + artist);
    }
    return null;
  }

  public Image getImage(AudioScrobblerAlbum album) {
    try {
      return NetworkUtils.getImage(NetworkUtils.getConnection(album.getCoverURL(), proxy));
    } catch (IOException e) {
      Log.debug("No image found for album " + album);
    }
    return null;
  }

  public Image getImage(AudioScrobblerArtist artist) {
    try {
      return NetworkUtils.getImage(NetworkUtils.getConnection(artist.getImageUrl(), proxy));
    } catch (IOException e) {
      Log.debug("No image found for artist " + artist);
    }
    return null;
  }

  public Image getImage(AudioScrobblerSimilarArtists similar) {
    try {
      return NetworkUtils.getImage(NetworkUtils.getConnection(similar.getPicture(), proxy));
    } catch (IOException e) {
      Log.debug("No image found for similar artist " + similar);
    }
    return null;
  }

  public Image getSmallImage(AudioScrobblerAlbum album) {
    try {
      return NetworkUtils.getImage(NetworkUtils.getConnection(album.getSmallCoverURL(), proxy));
    } catch (IOException e) {
      Log.debug("No small image found for album " + album);
    }
    return null;
  }

  private String getTopTag(Document xml) {
    Element el = (Element) xml.getElementsByTagName("toptags").item(0);
    NodeList tags = el.getElementsByTagName("tag");
    if (tags.getLength() > 0) {
      Element e = (Element) tags.item(0);
      return XMLUtils.getChildElementContent(e, "name");
    }
    return null;
  }
}
