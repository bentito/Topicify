package edu.dartmouth.topicify;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.att.android.speech.ATTSpeechError;
import com.att.android.speech.ATTSpeechError.ErrorType;
import com.att.android.speech.ATTSpeechErrorListener;
import com.att.android.speech.ATTSpeechResult;
import com.att.android.speech.ATTSpeechResultListener;
import com.att.android.speech.ATTSpeechService;
import edu.dartmouth.topicify.R;

public class SpeechService extends Activity {
    private Button sttButton = null;
    private Button recordButton = null;
    private Button topicButton = null;
    private TextView resultView = null;
    private MediaRecorder mRecorder = new MediaRecorder();
    private String oauthToken = null;
    private final String TESTAUDIOFILENAME = "/testaudiofile.amr";
    private String speechTxtInit = "";
    
    /** 
     * Called when the activity is first created.
    **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // create the speech text file with nothing in it.
        saveSpeechText("");
        // First, we specify which layout resource we'll be using.
        setContentView(R.layout.speech);
        
        sttButton = (Button)findViewById(R.id.stt_button);
        sttButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Log.v("Topicify", "Saving AMR audiofile");
    			mRecorder.stop();
				mRecorder.reset();
				mRecorder.release();
				recordButton.setText("Record");
                try {
					startSpeechService();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
        
        recordButton = (Button)findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                	if (recordButton.getText().equals("Record")) {
                		recordButton.setText("Pause");
                		recordAudioFile();
                	} else { // Pause pushed
            			mRecorder.stop();
        				mRecorder.reset();
        				mRecorder.release();
                		recordButton.setText("Record");
                        try {
        					startSpeechService();
        				} catch (IOException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
                	}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
            }
        });
        
        topicButton = (Button)findViewById(R.id.topicsButton);
        topicButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	postData();
            }
        });
        // This will show the recognized text.
        resultView = (TextView)findViewById(R.id.result);
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
    
    private String readSpeechText() {
    	String fileText = null;
    	File sdcard = Environment.getExternalStorageDirectory();

    	//Get the text file
    	File file = new File(sdcard,"speech.txt");

    	//Read text from file
    	StringBuilder text = new StringBuilder();

    	try {
    	    BufferedReader br = new BufferedReader(new FileReader(file));
    	    String line;

    	    while ((line = br.readLine()) != null) {
    	        text.append(line);
    	        text.append('\n');
    	    }
    	    br.close();
    	}
    	catch (IOException e) {
    	    //You'll need to add proper error handling here
    	}
    	fileText = text.toString();
    	return fileText;
    }

    private void saveSpeechText(String speechTxt) {
    	try
    	{
    		File sdcard = Environment.getExternalStorageDirectory();
    		File speechFile = new File(sdcard, "speech.txt");
    		if (!speechFile.exists())
    			speechFile.createNewFile();

    		BufferedWriter writer = new BufferedWriter(new FileWriter(speechFile, true /*append*/));
    		writer.write(speechTxt + " ");
    		writer.close();
    	}
    	catch (IOException e)
    	{
    		Log.e("Topicify", "Unable to write to the speech.txt file."+ e.getMessage());
    	}
    }

    public void postData() {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://lda.herokuapp.com/topics");
        
        httppost.setHeader("Accept", "*/*");
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httppost.setHeader("Expect", "100-continue");
        
        try {
            // Add your data
        	String speechText = readSpeechText();
            StringEntity se = new StringEntity(speechText, HTTP.UTF_8);
            se.setContentType("application/x-www-form-urlencoded");
        	httppost.setEntity(se);
        	
        	Header[] headers = httppost.getAllHeaders();
          	for (Header header : headers) {
        		Log.e("Topicify","Req: Key : " + header.getName() + ":" + header.getValue());
        	}
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            StatusLine status = response.getStatusLine();

            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
	
        	headers = response.getAllHeaders();
        	for (Header header : headers) {
        		Log.e("Topicify","Resp: Key : " + header.getName() + " ,Value : " + header.getValue());
        	}
            Log.e("Topicify", "Response status in postData:"+ status.toString());
            
            TextView topicView = (TextView)findViewById(R.id.topicsTextView);
            topicView.setMovementMethod(new ScrollingMovementMethod());
            topicView.setText(responseBody);

            Log.e("Topicify", "responseBody:"+ responseBody);
        } catch (UnsupportedEncodingException e) {
        	Log.e("Topicify", "Problem in postData: uee:"+ e.getMessage());
        } catch (ClientProtocolException e) {
        	Log.e("Topicify", "Problem in postData: cpe:"+ e.getMessage());
        } catch (IOException e) {
        	Log.e("Topicify", "Problem in postData: ioe:"+ e.getMessage());
        }
    } 
    
    private void recordAudioFile() throws IOException {
    	// start recording file as 3GP/AMR/NB (narrowband)
    	Log.v("Topicify", "Record Button Pushed");
    	mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		Log.v("Topicify", "Audio Source Set");
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
    }
    
    /** 
     * Starts the SpeechKit service with recorded audio file
     *	ResultListener gets the recognized text. ErrorListener gets recognition errors
     * @throws IOException 
    **/
    private void startSpeechService() throws IOException {
    	ATTSpeechService speechService = null;
		Log.v("Topicify", "Starting speech interaction");

    		speechService = ATTSpeechService.getSpeechService(this);

    		// Register for the success and error callbacks.
    		speechService.setSpeechResultListener(new ResultListener());
    		speechService.setSpeechErrorListener(new ErrorListener());

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

    		String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
    		mFileName += TESTAUDIOFILENAME;

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
                // There may be multiple results, but we will only use
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
        saveSpeechText(resultText);
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
        sttButton.setText(R.string.speak_wait);
        sttButton.setEnabled(false);
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
                sttButton.setText(R.string.stt);
                sttButton.setEnabled(true);
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
