package com.mobileapp.joyrhythm;

import java.util.*;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.*;

import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;
import com.mobileapp.joyrhythm.tools.Tools;
import com.mobileapp.joyrhythm.tools.ToolsTracker;
import com.mobileapp.joyrhythm.tools.ToolsUpdateTask;


public class MenuHome extends Activity {
	
	private static final int SELECT_MUSIC = 123;
	private static final String MENU_FONT = "fonts/HappyKiller.ttf";
	
	// Private variables
	//private AlertDialog alertDialog = null;
	private String title = "";
	private String backgroundPath = "";
	private boolean largeText = false;
	private String[] largeTextCountries= {"ko", "zh", "ru", "ja", "tr"};
	private static Locale defaultLocale;
	private Vibrator v;

    // Main screen
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.setContext(this);


        // Startup checks
        if (Tools.getBooleanSetting(R.string.resetSettings, R.string.resetSettingsDefault)) {
            Tools.resetSettings();
        }
        Tools.setScreenDimensions();
        setupLayout();

        updateCheck();
        versionCheck();
        showNotes();

        if (Tools.getBooleanSetting(R.string.additionalVibrations, R.string.additionalVibrationsDefault)) {
            v = ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
            v.vibrate(300); // ready to rumble!
        }

    }


    // Startup Warnings
	private void versionCheck() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR_MR1 &&
			!Tools.getBooleanSetting(R.string.ignoreLegacyWarning, R.string.ignoreLegacyWarningDefault)) {
			// Multitouch warning			
			Tools.warning(
					Tools.getString(R.string.MenuHome_legacy_warning),
					Tools.cancel_action, R.string.ignoreLegacyWarning
					);
		}
	}
	
	private void updateCheck() {
		new ToolsUpdateTask().execute(Tools.getString(R.string.Url_version));
	}
	

	private void showNotes() {
		
		// New User notes
		if (!Tools.getBooleanSetting(R.string.ignoreNewUserNotes, R.string.ignoreNewUserNotesDefault)) {
			
			DialogInterface.OnClickListener website_action = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					Tools.startWebsiteActivity(Tools.getString(R.string.Url_demo));
				}
			};
			
			DialogInterface.OnClickListener close_action = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			};
			
			Tools.note(
					Tools.getString(R.string.MenuHome_new_user_notes_title),
					R.drawable.icon_small,
					Tools.getString(R.string.MenuHome_new_user_notes),
					Tools.getString(R.string.Button_demo),
					website_action,
					Tools.getString(R.string.Button_close),
					close_action,
					R.string.ignoreNewUserNotes
					);
		}
		
		// Beta notes
		if (Tools.getBooleanSetting(R.string.App_version, R.string.betaNotesDefault) ||
			!Tools.getBooleanSetting(R.string.ignoreBetaNotes, R.string.ignoreBetaNotesDefault)) {
			// Beta warning
			DialogInterface.OnClickListener forums_action = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Tools.putSetting(R.string.App_version, "0");					
					dialog.cancel();
					Tools.startWebsiteActivity(Tools.getString(R.string.Url_updates));
				}
			};
			
			DialogInterface.OnClickListener close_action = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Tools.putSetting(R.string.App_version, "0");					
					dialog.cancel();
				}
			};
			
			
			Tools.note(
					Tools.getString(R.string.MenuHome_release_notes_title),
					R.drawable.icon_small,
					Tools.getString(R.string.MenuHome_release_notes),
					Tools.getString(R.string.Button_updates),
					forums_action,
					Tools.getString(R.string.Button_close),
					close_action,
					R.string.ignoreBetaNotes
					);
		}

		
		if (Tools.getBooleanSetting(R.string.installSamples, R.string.installSamplesDefault) ||
			!Tools.getBooleanSetting(R.string.ignoreNewUserNotes, R.string.ignoreNewUserNotesDefault)) {
			// Make folders and install sample songs
			if (Tools.isMediaMounted() && 
				Tools.makeBeatsDir()
				) {
				Tools.installSampleSongs(this);
				Tools.putSetting(R.string.installSamples, "0");
			}
		} else {
			// Always make folders
			if (Tools.isMediaMounted()) Tools.makeBeatsDir();
		}
	}
	
	// Activity Result
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (requestCode) {
			case SELECT_MUSIC:
				if (resultCode == RESULT_OK) {
					if (Tools.getBooleanSetting(R.string.autoStart, R.string.autoStartDefault)) {
						Tools.setContext(this);
						new MenuStartGame(this, title).startGameCheck();
					}
				}
				break;
			default:
				break;
		}
	}
	
	// Update displayed language
	public void updateLanguage() {
		if (defaultLocale == null) {
			defaultLocale = this.getResources().getConfiguration().locale;
		}
		
		String languageToLoad = Tools.getSetting(R.string.language, R.string.languageDefault);
		if (languageToLoad.equals("default")) {
			Configuration config = new Configuration();
			config.locale = defaultLocale;
			this.getResources().updateConfiguration(config, null);
		} else {
			Locale locale = new Locale(languageToLoad);
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			this.getResources().updateConfiguration(config, null);
		}
		
		// For non-roman alphabets
		String language = this.getResources().getConfiguration().locale.getLanguage();
		largeText = false;
		for (String country : largeTextCountries) {
			if (language.startsWith(country)) {
				largeText = true;
				break;
			}
		}
	}
	
	// Update layout images
	private void updateLayout() {
		updateLanguage();
		
		// Titlebar
		title = Tools.getString(R.string.MenuHome_titlebar) + " [" + Tools.getString(R.string.App_version) + "]";
		setTitle(title);
		
		// Menu items
		formatMenuItem(((TextView) findViewById(R.id.start)), R.string.Menu_start);
		formatMenuItem(((TextView) findViewById(R.id.select_song)), R.string.Menu_select_song);
		formatMenuItem(((TextView) findViewById(R.id.download_songs)), R.string.Menu_download_songs);
		formatMenuItem(((TextView) findViewById(R.id.settings)), R.string.Menu_settings);
		formatMenuItem(((TextView) findViewById(R.id.exit)), R.string.Menu_exit);
		
		updateDifficulty();
		
		updateAutoPlay();
		
		// Game Mode
		/*
		if (Tools.gameMode == Tools.OSU_MOD) {
			gameMode.setImageResource(R.drawable.icon_osu);
		} else {
			gameMode.setImageResource(R.drawable.icon_sm);
		}
		*/
		updateGameMode();
		
		// Background data icon
		/*
		ConnectivityManager cm =
			(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		ImageView backgroundData = (ImageView) findViewById(R.id.backgroundData);
		if (cm.getBackgroundDataSetting() &&
			!Tools.getBooleanSetting(R.string.ignoreSyncWarning, R.string.ignoreSyncWarningDefault)) {
			backgroundData.setVisibility(View.VISIBLE);
		} else {
			backgroundData.setVisibility(View.GONE);
		}
		*/
		
		// Background image
		String backgroundPathNew = Tools.getBackgroundRes();
		if (!backgroundPath.equals(backgroundPathNew)) {
			backgroundPath = backgroundPathNew;
			ImageView bg = (ImageView) findViewById(R.id.bg);
			try {
				Bitmap newBackground = BitmapFactory.decodeFile(backgroundPath);
				if (newBackground != null) {
					bg.setImageBitmap(newBackground);
				}
			} catch (Throwable t) {
				System.gc();
				ToolsTracker.error("MenuHome.updateLayout", t, "");
				Tools.toast_long(Tools.getString(R.string.MenuHome_background_image_load_fail));
			}
			System.gc();
		}
	}
	

	private void formatMenuItem(final TextView tv, int text) {
		Typeface tf = Typeface.createFromAsset(getAssets(), MENU_FONT);
		float textSize = 22f;
		if (largeText) {
			textSize += 6f;
		}
		if (Tools.tablet) {
			textSize += 26;
		}
		//textSize = Tools.scale(textSize);
		if (largeText) {
			//tv.setTypeface(tf, Typeface.BOLD);
			tv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		} else {
			tv.setTypeface(tf);
		}
		tv.setTextSize(textSize);
		tv.setTextColor(Color.BLACK);
		tv.setShadowLayer(5f, 0, 0, Color.WHITE);
		tv.setGravity(Gravity.CENTER);
		// We do this instead of ColorStateList since ColorStateList doesn't deal with shadows
		tv.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					tv.setTextColor(Color.WHITE);
					tv.setShadowLayer(9f, 0, 0, Color.BLACK);
				} else {
					tv.setTextColor(Color.BLACK);
					tv.setShadowLayer(7f, 0, 0, Color.WHITE);
				}
			}
		});
		tv.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent e) {
				if (e.getAction() == MotionEvent.ACTION_DOWN) {
					tv.setTextColor(Color.WHITE);
					tv.setShadowLayer(9f, 0, 0, Color.BLACK);
				} else if (e.getAction() == MotionEvent.ACTION_UP) {
					tv.setTextColor(Color.BLACK);
					tv.setShadowLayer(7f, 0, 0, Color.WHITE);
				}
				return false;
			}
		});
		tv.setText(text);
	}
	
	private void vibrate() {
		if (v != null) {
			v.vibrate(20);
		}
	}

	public void shareKakao(){
		try {
			final KakaoLink kakaoLink = KakaoLink.getKakaoLink(this);
			final KakaoTalkLinkMessageBuilder kakaoBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

			kakaoBuilder.addText("카카오링크 테스트");
            /*이미지 가로/세로 사이즈는 80px 보다 커야하며, 이미지 용량은 500kb 이하로 제한된다.*/
			String url = "https://www.google.co.kr/url?sa=i&rct=j&q=&esrc=s&source=images&cd=&cad=rja&uact=8&ved=0ahUKEwi_xbLYtIvUAhUEkJQKHaWmBSQQjRwIBw&url=http%3A%2F%2Fwww.myiconfinder.com%2Ficon%2Fconsole-control-game-games-joy-joystick-manipulator-joypad-pad-joy-color-4-flat%2F2421&psig=AFQjCNGilwX3CoypOmq4H9-LGMq2ID8tcw&ust=1495814307168093";
			kakaoBuilder.addImage(url, 512, 512);
    		kakaoBuilder.addAppButton("Go to Joy Rhythm");

            /*메시지 발송*/
			kakaoLink.sendMessage(kakaoBuilder, this);
		} catch (KakaoParameterException e){
			e.printStackTrace();
		}
	}

	private void setupLayout() {
		setContentView(R.layout.main);
		setVolumeControlStream(AudioManager.STREAM_MUSIC); // To control media volume at all times
		
		backgroundPath = ""; // Force background reload
		updateLayout();
		
		// Difficulty button
		final TextView difficulty = (TextView) findViewById(R.id.difficulty);
		difficulty.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				vibrate();
				//changeDifficulty();
				nextDifficulty();
			}
		});
		difficulty.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					difficulty.setBackgroundColor(Color.BLACK);
				} else {
					// Using the image increases the view's height and shifts the menu a bit,
					// so let's just forget about the background  
					//difficulty.setBackgroundResource(R.drawable.difficulty_header);
					difficulty.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});		
		
		// AutoPlay button
		TextView autoPlay = (TextView) findViewById(R.id.autoPlay);
		autoPlay.setTextColor(Color.RED);
		autoPlay.setShadowLayer(7f, 0, 0, Color.WHITE);
		autoPlay.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				vibrate();
				//changeAutoPlay();
				nextAutoPlay();
			}
		});
		
		// Game Mode
		ImageView gameModePrev = (ImageView) findViewById(R.id.gameModePrev);
		ImageView gameModeNext = (ImageView) findViewById(R.id.gameModeNext);
		final ImageView gameMode = (ImageView) findViewById(R.id.gameMode);
		
		int maxHeight = Tools.button_h * 2 / 3;
		gameMode.setAdjustViewBounds(true);
		gameMode.setMaxHeight(maxHeight);
		gameModePrev.setAdjustViewBounds(true);
		gameModePrev.setMaxHeight(maxHeight);
		gameModeNext.setAdjustViewBounds(true);
		gameModeNext.setMaxHeight(maxHeight);
		
		OnClickListener onGameModePrev = new OnClickListener() {
			public void onClick(View v) {
				vibrate();
				nextGameMode(true);
			}
		};
		gameModePrev.setOnClickListener(onGameModePrev);
		
		OnClickListener onGameModeNext = new OnClickListener() {
			public void onClick(View v) {
				vibrate();
				nextGameMode(false);
			}
		};
		gameMode.setOnClickListener(onGameModeNext);
		gameModeNext.setOnClickListener(onGameModeNext);
		
		gameMode.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					gameMode.setBackgroundColor(Color.BLACK);
				} else {
					gameMode.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});	
		
		/*
		// Background Data button
		ImageView backgroundData = (ImageView) findViewById(R.id.backgroundData);
		backgroundData.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showBackgroundData();
			}
		});
		*/
		
		// Start button
		TextView start_b = (TextView) findViewById(R.id.start);
		start_b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				vibrate();
				new MenuStartGame(MenuHome.this, title).startGameCheck();
			}
		});
		
		// Select Song button
		TextView select_song_b = (TextView) findViewById(R.id.select_song);
		select_song_b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				vibrate();
				if (Tools.isMediaMounted()) {
					Intent i = new Intent();
					i.setClass(MenuHome.this, MenuFileChooser.class);
					startActivityForResult(i, SELECT_MUSIC);
				}
			}
		});
		
		// Share button
		TextView shareButton = (TextView) findViewById(R.id.sharebutton);
		shareButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				shareKakao();
			}
		});
		
		// Download Songs button
		TextView download_songs_b = (TextView) findViewById(R.id.download_songs);
		download_songs_b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				vibrate();
				if (Tools.isMediaMounted()) {
					Tools.startWebsiteActivity(Tools.getString(R.string.Url_downloads));
				}
			}
		});
		
		// Exit button
		TextView exit_b = (TextView) findViewById(R.id.exit);
		exit_b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				vibrate();
				//backgroundDataUncheck();
				finish();
			}
		});

		// Settings button
		TextView settings_b = (TextView) findViewById(R.id.settings);
		settings_b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				vibrate();
				Intent i = new Intent();
				i.setClass(MenuHome.this, MenuSettings.class);
				startActivity(i);
			}
		});
		
		// Setup navigation for TVs/keyboard
		setupDpadNavigation();
	}
	
	private static int[] viewIds = {
		R.id.start,
		R.id.select_song,
		R.id.download_songs,
		R.id.settings,
		R.id.exit,
		R.id.difficulty,
		R.id.gameMode
	};
	
	private void setupDpadNavigation() {
		for (int i = 0; i < viewIds.length; i++) {
			View view = findViewById(viewIds[i]);
			view.setFocusable(true);
			
			int upIndex = i - 1;
			int downIndex = i + 1;
			if (i == 0) {
				upIndex = viewIds.length - 1;
			} else if (i == viewIds.length - 1) {
				downIndex = 0;
			}
			view.setNextFocusUpId(viewIds[upIndex]);
			view.setNextFocusLeftId(viewIds[upIndex]);
			view.setNextFocusDownId(viewIds[downIndex]);
			view.setNextFocusRightId(viewIds[downIndex]);
		}
		
		setupInitialFocus();
	}
	
	public void setupInitialFocus() {
		View firstView = findViewById(viewIds[0]);
		if (firstView != null) {
			firstView.requestFocus();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setupInitialFocus();
	}
	
	@Override
	public void onWindowFocusChanged (boolean hasFocus) {
		if (hasFocus) {
			Tools.setContext(this);
			updateLayout();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateLayout();
	}
	
	@Override
	protected void onDestroy() {
		ToolsTracker.stopTracking();
		super.onDestroy();
	}

	private void nextAutoPlay() {
		int autoPlay = Integer.parseInt(Tools.getSetting(R.string.autoPlay, R.string.autoPlayDefault));
		autoPlay++;
		if (autoPlay > 1) autoPlay = 0;
		Tools.putSetting(R.string.autoPlay, Integer.toString(autoPlay));
		updateAutoPlay();
	}
	
	private void updateAutoPlay() {
		// Header font
		Typeface tf = Typeface.createFromAsset(getAssets(), MENU_FONT);
		float textSize = 17f;
		if (largeText) {
			textSize += 3f;
		}
		if (Tools.tablet) {
			textSize += 20;
		}
		
		// AutoPlay header
		TextView autoPlay = (TextView) findViewById(R.id.autoPlay);
		if (largeText) {
			//autoPlay.setTypeface(tf, Typeface.BOLD);
			autoPlay.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		} else {
			autoPlay.setTypeface(tf);
		}
		if (Tools.getBooleanSetting(R.string.autoPlay, R.string.autoPlayDefault)) {
			autoPlay.setText(Tools.getString(R.string.Menu_auto));
		} else {
			autoPlay.setText("        ");
		}
		autoPlay.setTextSize(textSize);
	}
	
	// Ugly, won't fix
	private void nextDifficulty() {
		int difficulty = Integer.parseInt(Tools.getSetting(R.string.difficultyLevel, R.string.difficultyLevelDefault));
		difficulty++;
		if (difficulty > 4) difficulty = 0;
		Tools.putSetting(R.string.difficultyLevel, Integer.toString(difficulty));
		updateDifficulty();
	}
	
	private void updateDifficulty() {
		// Header font
		Typeface tf = Typeface.createFromAsset(getAssets(), MENU_FONT);
		float textSize = 17f;
		if (largeText) {
			textSize += 3f;
		}
		if (Tools.tablet) {
			textSize += 20;
		}
		//textSize = Tools.scale(textSize);
		
		// Difficulty header
		TextView difficulty = (TextView) findViewById(R.id.difficulty);
		if (largeText) {
			//difficulty.setTypeface(tf, Typeface.BOLD);
			difficulty.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		} else {
			difficulty.setTypeface(tf);
		}
		difficulty.setTextSize(textSize);
		switch (Integer.parseInt(
				Tools.getSetting(R.string.difficultyLevel, R.string.difficultyLevelDefault)
				)) {
			case 0:
				difficulty.setText(" " + Tools.getString(R.string.Difficulty_beginner).toLowerCase());
				difficulty.setTextColor(Color.rgb(255, 132, 0)); // orange
				break;
			case 1:
				difficulty.setText(" " + Tools.getString(R.string.Difficulty_easy).toLowerCase());
				difficulty.setTextColor(Color.rgb(0, 185, 255)); // light blue
				break;
			case 2:
				difficulty.setText(" " + Tools.getString(R.string.Difficulty_medium).toLowerCase());
				difficulty.setTextColor(Color.rgb(255, 0, 0)); // red
				break;
			case 3:
				difficulty.setText(" " + Tools.getString(R.string.Difficulty_hard).toLowerCase());
				difficulty.setTextColor(Color.rgb(32, 185, 32)); // green
				break;
			case 4:
				difficulty.setText(" " + Tools.getString(R.string.Difficulty_challenge).toLowerCase());
				difficulty.setTextColor(Color.rgb(14, 122, 230)); // dark blue
				break;
		}
	}
	
	// Ugly, won't fix
	private void nextGameMode(boolean prev) {
		int gameMode = Integer.parseInt(Tools.getSetting(R.string.gameMode, R.string.gameModeDefault));
		if (prev) {
			gameMode -= 1;
			if (gameMode < 0) gameMode = 3;
		} else {
			gameMode += 1;
			if (gameMode > 3) gameMode = 0;
		}
		Tools.putSetting(R.string.gameMode, Integer.toString(gameMode));
		updateGameMode();
	}
	
	private void updateGameMode() {
		Tools.updateGameMode();
		ImageView gameMode = (ImageView) findViewById(R.id.gameMode);
		switch(Tools.gameMode) {
			case Tools.REVERSE:
				gameMode.setImageResource(R.drawable.mode_step_down);
				break;
			case Tools.STANDARD:
				gameMode.setImageResource(R.drawable.mode_step_up);
				break;
			case Tools.OSU_MOD:
				if (Tools.randomizeBeatmap) { // OSU_MOD_RAND
					gameMode.setImageResource(R.drawable.mode_osu_rand);
				} else {
					gameMode.setImageResource(R.drawable.mode_osu);
				}
				break;
		}
	}

	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // Backward compatibility
			backgroundDataUncheck();
			return true;
		}
		*/
		switch (keyCode) {
			case KeyEvent.KEYCODE_MENU:
				//changeDifficulty();
				Intent i = new Intent();
				i.setClass(MenuHome.this, MenuSettings.class);
				startActivity(i);
				return true;
			case KeyEvent.KEYCODE_SEARCH:
				Tools.startWebsiteActivity(Tools.getString(R.string.Url_website));
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}
	}

}
