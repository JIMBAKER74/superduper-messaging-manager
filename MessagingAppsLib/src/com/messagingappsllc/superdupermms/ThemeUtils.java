package com.messagingappsllc.superdupermms;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.messagingappsllc.superdupermms.mms.GoTxtmeApp;
import com.messagingappsllc.superdupermms.mms.R;
import com.messagingappsllc.superdupermms.mms.ui.ConversationList;
import com.messagingappsllc.superdupermms.mms.ui.MessageListItem;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;

public class ThemeUtils {

	public static final String TAG = "ThemeUtils";
	private static ThemeUtils mInstance = null;
	private Context mContext;
	private Resources defRes;
	private ThemeResources themeRes, defaultRes;
	private List<ThemeListener> listeners;
	private boolean useDefault = true;
	private StateListDrawable sendBtnStateList = null;
	private Drawable dividerDrawable;
	
	private ThemeUtils(Context context, String packageName) {
		mContext = context;
		defRes = context.getResources();
		themeRes = new ThemeResources(packageName);
		defaultRes = new ThemeResources(GoTxtmeApp.getApplication().getPackageName());
		listeners = new ArrayList<ThemeListener>();
	}
	
	public static ThemeUtils getInstance() {
		if (mInstance == null) {
			mInstance = new ThemeUtils(GoTxtmeApp.getApplication(), GoTxtmeApp.getApplication().getPackageName());
	    	if (SDSmsConsts.DEBUG_INSTALL_RECEIVER) 
	    		Log.i("ThemeUtils", "mInstance GoTxtmeApp.getApplication().getPackageName():" + GoTxtmeApp.getApplication().getPackageName());
		}
		return mInstance;
	}
	
	public boolean showAds() {
		return mContext.getResources().getBoolean(R.bool.showAds);
	}
	
	public boolean hideActionsOnActionBar() {
		return mContext.getResources().getBoolean(R.bool.hideActionsOnActionBar);
	}
	
	public void addListener(ThemeListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ThemeListener listener) {
		listeners.remove(listener);
	}
	
	public boolean switchThemes(String packageName) {
		themeRes = new ThemeResources(packageName);
		if (themeRes.isReady()) {
			if (packageName.equals(GoTxtmeApp.getApplication().getPackageName()) ||
					packageName.equals(ConversationList.DEFAULT_THEME_SELECTED)) useDefault = true;
			else useDefault = false;
			for (ThemeListener listener : listeners) {
				listener.updateTheme();
			}
		}
		return themeRes.isReady();
	}
	
	public boolean useDefaultTheme(boolean useDef) {
		if (useDefault ^ useDef) {
			if (!useDef && themeRes != null) useDefault = useDef;
			else if (useDef) useDefault = useDef; 
			else return useDefault;
			if (themeRes.isReady()) for (ThemeListener listener : listeners) {
				listener.updateTheme();
			}
		}
		return useDefault;
	}
	
	public ThemeResources getTheme() {
		if (useDefault) return defaultRes;
		return themeRes;
	}
	
	public class ThemeResources {
		private Resources res;
		private String mPackageName;
		private boolean found = false;
		
		public ThemeResources(String packageName) {
			try {
				mPackageName = packageName;
				res = mContext.getPackageManager().getResourcesForApplication(packageName);
				found = true;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				found = false;
			}
		}
		
		public void updateSendButton(ImageButton sendBtn) {
			if (useDefault) {
				sendBtn.setImageDrawable(defRes.getDrawable(R.drawable.send_button_selector));
				return;
			}
			StateListDrawable states = new StateListDrawable();
			states.addState(new int[] {-android.R.attr.state_enabled},
			    res.getDrawable(res.getIdentifier("ic_send_disabled_holo_light", "drawable", mPackageName)));
			states.addState(new int[] { },
			    res.getDrawable(res.getIdentifier("ic_send_holo_light", "drawable", mPackageName)));
			sendBtn.setImageDrawable(states);
		}
		
		public void updateSendMmsButton(TextView sendBtn) {
			if (useDefault) {
				for(Drawable myOldDrawable : sendBtn.getCompoundDrawables()) {
					if (!(myOldDrawable == null)) myOldDrawable.setCallback(null);
				}

				//use null where you don't want a drawable
				sendBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, 
						defRes.getDrawable(R.drawable.send_button_selector));
				return;
			}
			StateListDrawable states = new StateListDrawable();
			states.addState(new int[] {-android.R.attr.state_enabled},
			    res.getDrawable(res.getIdentifier("ic_send_disabled_holo_light", "drawable", mPackageName)));
			states.addState(new int[] { },
			    res.getDrawable(res.getIdentifier("ic_send_holo_light", "drawable", mPackageName)));
			for(Drawable myOldDrawable : sendBtn.getCompoundDrawables()) {
			   if (!(myOldDrawable == null)) myOldDrawable.setCallback(null);
			}
			
			//use null where you don't want a drawable
			sendBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, states);
		}
		
		public void updateSendDateTxt(TextView dateText) {
			if (useDefault) {
				dateText.setTextColor(defRes.getColor(R.color.text_hairline));
				dateText.setBackground(defRes.getDrawable(R.drawable.listitem_background));
			} else {
				try {
					dateText.setTextColor(res.getColor(res.getIdentifier("text_hairline", "color", mPackageName)));
					dateText.setBackground(res.getDrawable(res.getIdentifier("listitem_send_background", "drawable", mPackageName)));
				} catch (Exception e) {
					dateText.setTextColor(defRes.getColor(R.color.text_hairline));
					dateText.setBackground(defRes.getDrawable(R.drawable.listitem_background));
				}
			}
		}
		
		public void updateRecvDateTxt(TextView dateText) {
			if (useDefault) {
				dateText.setTextColor(defRes.getColor(R.color.text_hairline));
				dateText.setBackground(defRes.getDrawable(R.drawable.listitem_background));
			} else {
				try {
					dateText.setTextColor(res.getColor(res.getIdentifier("text_hairline", "color", mPackageName)));
					dateText.setBackground(res.getDrawable(res.getIdentifier("listitem_receive_background", "drawable", mPackageName)));
				} catch (Exception e) {
					dateText.setTextColor(defRes.getColor(R.color.text_hairline));
					dateText.setBackground(defRes.getDrawable(R.drawable.listitem_background));
				}
			}
		}
		
		public int getMsgCountColor() {
			if (useDefault) {
				return defRes.getColor(R.color.message_count_color);
			}
			return res.getColor(res.getIdentifier("message_count_color", "color", mPackageName));
		}
		
		public void updateEditText(EditText editText) {
			if (useDefault) {
				editText.setBackground(defRes.getDrawable(R.drawable.edit_text_holo_light));
				editText.setTextColor(defRes.getColor(R.color.compose_edit_text_color));
			} else {
				try {
					StateListDrawable states = new StateListDrawable();
					states.addState(new int[] {-android.R.attr.state_window_focused, android.R.attr.state_enabled},
						    res.getDrawable(res.getIdentifier("textfield_multiline_default_holo_light", "drawable", mPackageName)));
					states.addState(new int[] {-android.R.attr.state_window_focused, -android.R.attr.state_enabled},
						    res.getDrawable(res.getIdentifier("textfield_multiline_default_holo_light", "drawable", mPackageName)));
					states.addState(new int[] {android.R.attr.state_focused},
						    res.getDrawable(res.getIdentifier("textfield_multiline_focused_holo_light", "drawable", mPackageName)));
					states.addState(new int[] {android.R.attr.state_activated},
						    res.getDrawable(res.getIdentifier("textfield_multiline_activated_holo_light", "drawable", mPackageName)));
					states.addState(new int[] {android.R.attr.state_enabled},
						    res.getDrawable(res.getIdentifier("textfield_multiline_default_holo_light", "drawable", mPackageName)));
					states.addState(new int[] {android.R.attr.state_focused},
						    res.getDrawable(res.getIdentifier("textfield_multiline_disabled_focused_holo_light", "drawable", mPackageName)));
					states.addState(new int[] { },
					    res.getDrawable(res.getIdentifier("textfield_multiline_disabled_holo_light", "drawable", mPackageName)));
					editText.setBackground(states);
				} catch (Exception e) {
					editText.setBackground(defRes.getDrawable(R.drawable.edit_text_holo_light));
				}
				try {
					editText.setTextColor(
							res.getColor(res.getIdentifier("compose_edit_text_color", "color", mPackageName)));
				} catch (Exception e) {
					editText.setTextColor(defRes.getColor(R.color.compose_edit_text_color));
				}
			}
		}
		
		public Drawable getIconImg() {
			if (useDefault) return defRes.getDrawable(R.mipmap.ic_launcher_smsmms);
			else return res.getDrawable(res.getIdentifier("ic_launcher_smsmms", "mipmap", mPackageName));
		}
		
		public Drawable getActionBarIconImg() {
			if (mContext.getResources().getBoolean(R.bool.alternateActionBarIcon)) {
				if (useDefault) return defRes.getDrawable(R.drawable.actionbar_icon);
				else try {
					return res.getDrawable(res.getIdentifier("actionbar_icon", "drawable", mPackageName));
				} catch (Exception e) {
					return defRes.getDrawable(R.drawable.actionbar_icon);
				}
			}
			if (useDefault) return defRes.getDrawable(R.mipmap.ic_launcher_smsmms);
			else return res.getDrawable(res.getIdentifier("ic_launcher_smsmms", "mipmap", mPackageName));
		}
		
		public Drawable getComposeMessageBackgroundImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.compose_bg);
			else return res.getDrawable(res.getIdentifier("compose_bg", "drawable", mPackageName));
		}
		
		public int getConversationListActionBarColor() {
			if (useDefault) return defRes.getColor(R.color.contact_action_bar_color);
			else try {
				return res.getColor(res.getIdentifier("contact_action_bar_color", "color", mPackageName));
			} catch (Exception e) {
				return defRes.getColor(R.color.contact_action_bar_color);
			}
		}
		
		public void setConversationListDividerDrawable(Drawable divider) {
			dividerDrawable = divider;
		}
		
		public Drawable getConversationListDividerDrawable() {
			
			if (useDefault) return dividerDrawable;
			else {
				try {
					if (res.getBoolean(res.getIdentifier("use_divider_color", "bool", mPackageName))) {
						int[] retval = new int[3];
						try {
							retval[0] = res.getColor(res.getIdentifier("divider_left_color", "color", mPackageName));
						} catch (Exception e) {
							retval[0] = 0;
						}
						try {
							retval[1] = res.getColor(res.getIdentifier("divider_center_color", "color", mPackageName));
						} catch (Exception e) {
							retval[1] = 0;
						}
						try {
							retval[2] = res.getColor(res.getIdentifier("divider_right_color", "color", mPackageName));
						} catch (Exception e) {
							retval[2] = 0;
						}
						return new GradientDrawable(Orientation.RIGHT_LEFT, retval);
					} else return dividerDrawable;
				} catch (Exception e) {
					return dividerDrawable;
				}
			}
		}
		
		public int getContactListBackgroundColor() {
			if (useDefault) return defRes.getColor(R.color.instWhite);
			else try {
				return res.getColor(res.getIdentifier("contact_list_color", "color", mPackageName));
			} catch (Exception e) {
				return defRes.getColor(R.color.instWhite);
			}
		}
		
		public int getContactListItemBackgroundColor() {
			if (useDefault) return defRes.getColor(R.color.instWhite);
			else try {
				return res.getColor(res.getIdentifier("contact_list_item_color", "color", mPackageName));
			} catch (Exception e) {
				return defRes.getColor(R.color.instWhite);
			}
		}
		
		public int getBottomLayoutColor() {
			if (useDefault) return defRes.getColor(R.color.background);
			else try {
				return res.getColor(res.getIdentifier("bottom_panel_background", "color", mPackageName));
			} catch (Exception e) {
				return defRes.getColor(R.color.background);
			}
		}
		
		public int getSendMsgTextColor() {
			if (useDefault) return defRes.getColor(R.color.message_sent);
			else try {
				return res.getColor(res.getIdentifier("message_sent", "color", mPackageName));
			} catch (Exception e) {
				return defRes.getColor(R.color.message_sent);
			}
		}
		
		public int getRecvMsgTextColor() {
			if (useDefault) return defRes.getColor(R.color.message_received);
			else try {
				return res.getColor(res.getIdentifier("message_received", "color", mPackageName));
			} catch (Exception e) {
				return defRes.getColor(R.color.message_received);
			}
		}
		
		public Drawable getComposeMessageLogoImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.img_bg_logo);
			else return res.getDrawable(res.getIdentifier("img_bg_logo", "drawable", mPackageName));
		}
		
		public Drawable getContactImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.ic_contact_picture);
			else return res.getDrawable(res.getIdentifier("ic_contact_picture", "drawable", mPackageName));
		}
		
		public int getContactImgRes() {
			if (useDefault) return R.drawable.ic_contact_picture;
			else return res.getIdentifier("ic_contact_picture", "drawable", mPackageName);
		}
		
		public Drawable getSentContactImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.ic_contact_picture);
			else return res.getDrawable(res.getIdentifier("ic_contact_picture_sent", "drawable", mPackageName));
		}
		
		public int getSentContactImgRes() {
			if (useDefault) return R.drawable.ic_contact_picture;
			else return res.getIdentifier("ic_contact_picture_sent", "drawable", mPackageName);
		}
		
		public Drawable getMenuCallImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.ic_menu_call);
			else return res.getDrawable(res.getIdentifier("ic_menu_call", "drawable", mPackageName));
		}
		
		public Drawable getMenuComposeImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.ic_menu_msg_compose_holo_dark);
			else return res.getDrawable(res.getIdentifier("ic_menu_msg_compose_holo_dark", "drawable", mPackageName));
		}
		
		public Drawable getMenuAttachImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.ic_menu_attachment);
			else return res.getDrawable(res.getIdentifier("ic_menu_attachment", "drawable", mPackageName));
		}
		
		public Drawable getRecipientAddImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.ic_recipients_add);
			else return res.getDrawable(res.getIdentifier("ic_recipients_add", "drawable", mPackageName));
		}
		
		public Drawable getMenuSearchImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.ic_menu_search_holo_dark);
			else return res.getDrawable(res.getIdentifier("ic_menu_search_holo_dark", "drawable", mPackageName));
		}
		
		public Drawable getBubbleLeftImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.msg_bubble_left);
			else return res.getDrawable(res.getIdentifier("msg_bubble_left", "drawable", mPackageName));
		}
		
		public Drawable getBubbleRightImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.msg_bubble_right);
			else return res.getDrawable(res.getIdentifier("msg_bubble_right", "drawable", mPackageName));
		}
		
		public Drawable getMsgSentBackgroundImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.hairline_right);
			else return res.getDrawable(res.getIdentifier("hairline_right", "drawable", mPackageName));
		}
		
		public Drawable getMsgReceiveBackgroundImg() {
			if (useDefault) return defRes.getDrawable(R.drawable.hairline_left);
			else return res.getDrawable(res.getIdentifier("hairline_left", "drawable", mPackageName));
		}
		
		public boolean showAvatar() {
			if (useDefault) return defRes.getBoolean(R.bool.showAvatar);
			else try {
				return res.getBoolean(res.getIdentifier("showAvatar", "bool", mPackageName));
			} catch (Exception e) {
				return defRes.getBoolean(R.bool.showAvatar);
			}
		}
		
		public boolean fillMsgWidth() {
			if (useDefault) return defRes.getBoolean(R.bool.fillMsgWidth);
			else try {
				return res.getBoolean(res.getIdentifier("fillMsgWidth", "bool", mPackageName));
			} catch (Exception e) {
				return defRes.getBoolean(R.bool.fillMsgWidth);
			}
		}
		
		public int getAvatarWidthHeight() {
			return defRes.getDimensionPixelSize(R.dimen.avatar_width_height);
		}
		
		public void setMessageItemPadding(MessageListItem msg) {
			if (useDefault) msg.setPadding(
					defRes.getDimensionPixelSize(R.dimen.leftMsgPadding),
					defRes.getDimensionPixelSize(R.dimen.topMsgPadding),
					defRes.getDimensionPixelSize(R.dimen.rightMsgPadding),
					defRes.getDimensionPixelSize(R.dimen.bottomMsgPadding));
			else {
				try {
					msg.setPadding(
							res.getDimensionPixelSize(res.getIdentifier("leftMsgPadding", "dimen", mPackageName)),
							res.getDimensionPixelSize(res.getIdentifier("topMsgPadding", "dimen", mPackageName)),
							res.getDimensionPixelSize(res.getIdentifier("rightMsgPadding", "dimen", mPackageName)),
							res.getDimensionPixelSize(res.getIdentifier("bottomMsgPadding", "dimen", mPackageName)));
				} catch (Exception e) {
					msg.setPadding(
							defRes.getDimensionPixelSize(R.dimen.leftMsgPadding),
							defRes.getDimensionPixelSize(R.dimen.topMsgPadding),
							defRes.getDimensionPixelSize(R.dimen.rightMsgPadding),
							defRes.getDimensionPixelSize(R.dimen.bottomMsgPadding));
				}
			}
		}
		
		public boolean isReady() {
			return found;
		}
		
		public String getPackageName() {
			return mPackageName;
		}
	}
	
	public interface ThemeListener {
		public void updateTheme();
	}
}
