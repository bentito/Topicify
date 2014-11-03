/*
Licensed by AT&T under 'Software Development Kit Tools Agreement' 2012.
TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION: http://developer.att.com/sdk_agreement/
Copyright 2012 AT&T Intellectual Property. All rights reserved. 
For more information contact developer.support@att.com http://developer.att.com
*/
package edu.dartmouth.topicify;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.att.android.speech.ATTSpeechError;
import com.att.android.speech.ATTSpeechError.ErrorType;
import com.att.android.speech.ATTSpeechErrorListener;
import com.att.android.speech.ATTSpeechResult;
import com.att.android.speech.ATTSpeechResultListener;
import com.att.android.speech.ATTSpeechService;
import edu.dartmouth.topicify.R;

/**
 * SimpleSpeech is a very basic demonstration of using the ATTSpeechKit 
 * to do voice recognition.  It is designed to introduce a developer to making 
 * a new application that uses the AT&T SpeechKit Android library.  
 * It also documents some of the more basic Android methods for those developers 
 * that are new to Android as well.
 * 
 * This version of the sample code shows how to call ATTSpeechService.
**/
public class SpeechService extends Activity {
    private Button recordButton = null;
    private Button sttButton = null;
    private TextView resultView = null;
    private WebView webView = null;
    private String oauthToken = null;
    private final String TESTAUDIOFILENAME = "/testaudiofile.amr";
    
    /** 
     * Called when the activity is first created.  This is where we'll hook up 
     * our views in XML layout files to our application.
    **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // First, we specify which layout resource we'll be using.
        setContentView(R.layout.speech);
        
        recordButton = (Button)findViewById(R.id.stt_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
					startSpeechService();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
        
        sttButton = (Button)findViewById(R.id.record_button);
        sttButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
					recordAudioFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
        
        // This will show the recognized text.
        resultView = (TextView)findViewById(R.id.result);
        
        // This will show a website receiving the recognized text.
        webView = (WebView)findViewById(R.id.webview);
        configureWebView();
    }
    
    /** 
     * Called when the activity is coming to the foreground.
     * This is where we will fetch a fresh OAuth token.
    **/
    @Override
    protected void onStart() {
    	Log.v("Topicify", "onStart of SpeechService");
        super.onStart();
        
        // Fetch the OAuth credentials.  
        validateOAuth();
    }

    private void recordAudioFile() throws IOException {
        // TODO work here on getting correct audio version of bytes that AT&T wants
        // trying: record file as 3GP/AMR/WB (wideband), read same file back in as byte array and submit to speech-to-text service
    	Log.v("Topicify", "Record Button Pushed");
		final MediaRecorder mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += TESTAUDIOFILENAME;
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e("AUDIO", "prepare() failed");
		}
		mRecorder.start();
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mRecorder.stop();
						mRecorder.reset();
						mRecorder.release();
					}
				});

			}
		}, 5000);
		Log.v("Topicify", "Saving AMR audiofile");
		// Just trying to call it a WAV file so far
    }
    
    /** 
     * Called by the Speak button in the sample activity.
     * Starts the SpeechKit service that listens to the microphone and returns
     * the recognized text.
     * @throws IOException 
    **/
    private void startSpeechService() throws IOException {
        // The ATTSpeechKit uses a singleton object to interface with the 
        // speech server.
        ATTSpeechService speechService = ATTSpeechService.getSpeechService(this);
        
        // Register for the success and error callbacks.
        speechService.setSpeechResultListener(new ResultListener());
        speechService.setSpeechErrorListener(new ErrorListener());
        // Next, we'll put in some basic parameters.
        // First is the Request URL.  This is the URL of the speech recognition 
        // service that you were given during onboarding.
        try {
            speechService.setRecognitionURL(new URI(SpeechConfig.serviceUrl()));
        }
        catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        
        // Specify the speech context for this app.
        speechService.setSpeechContext("Generic");
        
        // Set the OAuth token that was fetched in the background.
        speechService.setBearerAuthToken(oauthToken);
        
        // Add extra arguments for speech recognition.
        // The parameter is the name of the current screen within this app.
        speechService.setXArgs(
                Collections.singletonMap("ClientScreen", "main"));

        // Finally we have all the information needed to start the speech service.  
//        speechService.startListening();
//        Log.v("SimpleSpeech", "Starting speech interaction");
		String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += TESTAUDIOFILENAME;
//        byte [] audioDataFromWAV = IOUtil.readFile(mFileName);
        byte [] audioData = IOUtil.readBufferedFile(mFileName);
        
        speechService.setContentType("audio/amr");
        speechService.setShowUI(false);
        
		speechService.startWithAudioData(audioData);
        Log.v("Topicify", "Sending amr audiofile");
    }
    
    /**
     * This callback object will get all the speech success notifications.
    **/
    private class ResultListener implements ATTSpeechResultListener {
        public void onResult(ATTSpeechResult result) {
            // The hypothetical recognition matches are returned as a list of strings.
            List<String> textList = result.getTextStrings();
            String resultText = null;
            if (textList != null && textList.size() > 0) {
                // There may be multiple results, but this example will only use
                // the first one, which is the most likely.
                resultText = textList.get(0);
            }
            if (resultText != null && resultText.length() > 0) {
                // This is where your app will process the recognized text.
                Log.v("Topicify", "Recognized "+textList.size()+" hypotheses.");
                handleRecognition(resultText);
            }
            else {
                // The speech service did not recognize what was spoken.
                Log.v("Topicify", "Recognized no hypotheses.");
                alert("Didn't recognize speech", "Please try again.");
            }
        }
    }
    
    /** Make use of the recognition text in this app. **/
    private void handleRecognition(String resultText) {
        // In this example, we set display the text in the result view.
        resultView.setText(resultText);
        // And then perform a search on a website using the text.
        String query = URLEncoder.encode(resultText);
        String url = "http://en.m.wikipedia.org/w/index.php?search="+query;
        webView.loadUrl(url);
    }
    
    /** Configure the webview that displays websites with the recognition text. **/
    private void configureWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false; // Let the webview display the URL
            }
        });
    }

    /**
     * This callback object will get all the speech error notifications.
    **/
    private class ErrorListener implements ATTSpeechErrorListener {
        public void onError(ATTSpeechError error) {
            ErrorType resultCode = error.getType();
            if (resultCode == ErrorType.USER_CANCELED) {
                // The user canceled the speech interaction.
                // This can happen through several mechanisms:
                // pressing a cancel button in the speech UI;
                // pressing the back button; starting another activity;
                // or locking the screen.
                
                // In all these situations, the user was instrumental
                // in canceling, so there is no need to put up a UI alerting 
                // the user to the fact.
                Log.v("Topicify", "User canceled.");
            }
            else {
                // Any other value for the result code means an error has occurred.
                // The argument includes a message to help the programmer 
                // diagnose the issue.
                String errorMessage = error.getMessage();
                Log.v("Topicify", "Recognition error #"+resultCode+": "+errorMessage);
                
                alert("Speech Error", "Please try again later.");
            }
        }
    }

    /**
     * Start an asynchronous OAuth credential check. 
     * Disables the Speak button until the check is complete.
    **/
    private void validateOAuth() {
        SpeechAuth auth = 
            SpeechAuth.forService(SpeechConfig.oauthUrl(), SpeechConfig.oauthScope(), 
                SpeechConfig.oauthKey(), SpeechConfig.oauthSecret());
        auth.fetchTo(new OAuthResponseListener());
        recordButton.setText(R.string.speak_wait);
        recordButton.setEnabled(false);
    }
    
    /**
     * Handle the result of an asynchronous OAuth check.
    **/
    private class OAuthResponseListener implements SpeechAuth.Client {
        public void 
        handleResponse(String token, Exception error)
        {
            if (token != null) {
                oauthToken = token;
                recordButton.setText(R.string.stt);
                recordButton.setEnabled(true);
            }
            else {
                Log.v("Topicify", "OAuth error: "+error);
                // There was either a network error or authentication error.
                // Show alert for the latter.
                alert("Speech Unavailable", 
                    "This app was rejected by the speech service.  Contact the developer for an update.");
            }
        }
    }

    private void alert(String header, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
            .setTitle(header)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
