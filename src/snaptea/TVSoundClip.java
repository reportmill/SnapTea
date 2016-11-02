package snaptea;
import java.io.IOException;
import org.teavm.jso.dom.html.HTMLAudioElement;
import org.teavm.jso.dom.html.HTMLDocument;
import snap.gfx.SoundClip;
import snap.web.WebURL;

/**
 * A custom class.
 */
public class TVSoundClip extends SoundClip {
    
    // The Audio Element
    HTMLAudioElement   _snd, _snd2;
    
/**
 * Creates a new TVSoundClip.
 */
public TVSoundClip(Object aSource)
{
    WebURL url = WebURL.getURL(aSource);
    _snd = HTMLDocument.current().createElement("audio").withAttr("src", url.getPath().substring(1))
    .withAttr("preload", "auto").cast();
    _snd.load();
}

/**
 * Loads image synchronously with wait/notify.
 */
private synchronized void loadSound(WebURL aURL)
{
    _snd = HTMLDocument.current().createElement("audio").withAttr("src", aURL.getPath().substring(1))
    .withAttr("preload", "auto").cast();
    _snd.load();
}

/**
 * Returns whether sound is playing.
 */
public boolean isPlaying()  { return _snd2!=null && !_snd2.isEnded(); }

/**
 * Plays the sound.
 */
public void play()
{
    _snd2 = (HTMLAudioElement)_snd.cloneNode(false);
    _snd2.play();
}

/**
 * Plays the sound repeatedly for given count.
 */
public void play(int aCount)  { }

/**
 * Tells sound to stop playing.
 */
public void stop()  { }

/**
 * Pauses a sound.
 */
public void pause()  { }

/**
 * Starts a recording.
 */
public void recordStart()  { }

/**
 * Stops a recording.
 */
public void recordStop()  { }

/**
 * Returns whether sound is recording.
 */
public boolean isRecording()  { return false; }

/**
 * Returns the sound length in milliseconds.
 */
public int getLength()  { return 0; }

/**
 * Returns the sound time in milliseconds.
 */
public int getTime()  {  return 0; }

/**
 * Sets the sound time in milliseconds.
 */
public void setTime(int aTime)  { }

/**
 * Saves this sound.
 */
public void save() throws IOException  { }

}