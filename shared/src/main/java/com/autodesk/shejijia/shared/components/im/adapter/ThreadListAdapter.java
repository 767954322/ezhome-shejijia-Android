package com.autodesk.shejijia.shared.components.im.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.appglobal.Constant;
import com.autodesk.shejijia.shared.components.common.uielements.CircleImageView;
import com.autodesk.shejijia.shared.components.common.utility.DateUtil;
import com.autodesk.shejijia.shared.components.common.utility.GsonUtil;
import com.autodesk.shejijia.shared.components.common.utility.ImageUtils;
import com.autodesk.shejijia.shared.components.common.utility.LogUtils;
import com.autodesk.shejijia.shared.components.im.constants.MPChatConstants;
import com.autodesk.shejijia.shared.components.im.datamodel.Body;
import com.autodesk.shejijia.shared.components.im.datamodel.MPChatCommandInfo;
import com.autodesk.shejijia.shared.components.im.datamodel.MPChatEntityData;
import com.autodesk.shejijia.shared.components.im.datamodel.MPChatMessage;
import com.autodesk.shejijia.shared.components.im.datamodel.MPChatThread;
import com.autodesk.shejijia.shared.components.im.datamodel.MPChatUser;
import com.autodesk.shejijia.shared.components.im.datamodel.MPChatUtility;
import com.autodesk.shejijia.shared.components.im.widget.RoundImageViewGroup;
import com.autodesk.shejijia.shared.framework.AdskApplication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ThreadListAdapter extends BaseAdapter {
    public interface ThreadListAdapterInterface {
        int getThreadCount();

        MPChatThread getThreadObjectForIndex(int index);

        Boolean isUserConsumer();

        int getLoggedInUserId();

        String getChatListType(); //获取聊天列表类型
    }


    public ThreadListAdapter(Context context, ThreadListAdapterInterface threadListInterface, boolean isFileBased) {
        mContext = context;
        mThreadListInterface = threadListInterface;
        mIsFileBased = isFileBased;
    }


    public int getCount() {
        return mThreadListInterface.getThreadCount();
    }


    public MPChatThread getItem(int position) {
        return mThreadListInterface.getThreadObjectForIndex(position);
    }


    public long getItemId(int position) {
        return position;
    }


    public int getLayoutId() {
        if (mIsFileBased) {
            return R.layout.view_thread_list_row_for_file;
        } else {
            if (mThreadListInterface.getChatListType().equalsIgnoreCase(MPChatConstants.BUNDLE_VALUE_GROUP_CHAT_LIST)) {
                return R.layout.view_thread_list_row_for_group;
            } else {
                return R.layout.view_thread_list_row;
            }
        }

    }

    public void hideUnreadCountForRow(View view) {
        View unreadMessageCountView = view.findViewById(R.id.tv_unread_message_count);

        if (unreadMessageCountView != null)
            unreadMessageCountView.setVisibility(View.GONE);

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(getLayoutId(), null);
        ViewHolder holder = initHolder(convertView);
        loadData(holder, position);
        return convertView;
    }


    private boolean isUserConsumer() {
        return mThreadListInterface.isUserConsumer();
    }


    private ViewHolder initHolder(View container) {
        ViewHolder viewHolder = new ViewHolder();
        if (mIsFileBased) {
            viewHolder.fileThumbnail = (ImageView) container.findViewById(R.id.head_ico);
        } else {
            if (mThreadListInterface.getChatListType().equalsIgnoreCase(MPChatConstants.BUNDLE_VALUE_GROUP_CHAT_LIST)) {
                viewHolder.imageViewGroup = (RoundImageViewGroup) container.findViewById(R.id.head_ico);
            } else {
                viewHolder.userThumbnail = (CircleImageView) container.findViewById(R.id.head_ico);
            }
        }

        viewHolder.unreadMessageCount = (TextView) container.findViewById(R.id.tv_unread_message_count);
        viewHolder.name = (TextView) container.findViewById(R.id.tv_name);
        viewHolder.description = (TextView) container.findViewById(R.id.tv_content);
        viewHolder.time = (TextView) container.findViewById(R.id.tv_time);
        return viewHolder;
    }


    private void loadData(ViewHolder holder, int position) {
        MPChatThread thread = getItem(position);
        MPChatUser sender = MPChatUtility.getComplimentryUserFromThread(thread, "" + mThreadListInterface.getLoggedInUserId());
        if (thread.unread_message_count > 0) {
            (holder).unreadMessageCount.setText(String.valueOf(thread.unread_message_count));
            (holder).unreadMessageCount.setVisibility(View.VISIBLE);
        } else {
            (holder).unreadMessageCount.setVisibility(View.GONE);
        }

        if (mThreadListInterface.getChatListType().equalsIgnoreCase(MPChatConstants.BUNDLE_VALUE_GROUP_CHAT_LIST)){
            if (!TextUtils.isEmpty(getProjectName(thread))) {
                (holder).name.setText(getProjectName(thread));
            }
        }else {
            (holder).name.setText(getDisplayName(thread));
        }

        Date date = DateUtil.acsDateToDate(thread.latest_message.sent_time);

        if (date != null)
            (holder).time.setText(DateUtil.formattedStringFromDateForChatList(mContext, date));
        else
            (holder).time.setText(DateUtil.getTimeMY(thread.latest_message.sent_time));

        MPChatMessage.eMPChatMessageType message_media_type = thread.latest_message.message_media_type;
        LogUtils.d("message_type",message_media_type.name());
        switch (message_media_type) {
            case eTEXT:
                (holder).description.setText(thread.latest_message.body);
                break;
            case eIMAGE:
                (holder).description.setText(R.string.picture_msg);
                break;
            case eAUDIO:
                (holder).description.setText(R.string.audio_msg);
                break;
            case eCOMMAND: {
                if (thread.latest_message.command.equalsIgnoreCase("command")) {
                    MPChatCommandInfo info = MPChatMessage.getCommandInfoFromMessage(thread.latest_message);

                    if (info == null || (info.for_consumer == null && info.for_designer == null)) {
                        break;
                    }

                    if (isUserConsumer())
                        (holder).description.setText(info.for_consumer);
                    else
                        (holder).description.setText(info.for_designer);
                } else if (thread.latest_message.command.equalsIgnoreCase("CHAT_ROLE_ADD")) {
                    (holder).description.setText(thread.latest_message.body);
                } else if (thread.latest_message.command.equalsIgnoreCase("CHAT_ROLE_REMOVE")) {
                    (holder).description.setText(thread.latest_message.body);
                } else if (thread.latest_message.command.equalsIgnoreCase("CHAT_WELCOME_MESSAGE")){
                    if (!TextUtils.isEmpty(thread.latest_message.body)){
                        (holder).description.setText(thread.latest_message.body);
                    }
                }else {
                    //thread.latest_message.command为NULL
                    if (thread.latest_message.body != null) {
                        String user_type = AdskApplication.getInstance().getMemberEntity().getMember_type();
                        String body = thread.latest_message.body;
                        try {
                            Body body_entity = GsonUtil.jsonToBean(body, Body.class);
                            if (user_type.equals(Constant.UerInfoKey.DESIGNER_TYPE)) {
                                (holder).description.setText(body_entity.getFor_designer() + "");
                            } else if (user_type.equals(Constant.UerInfoKey.CONSUMER_TYPE)) {
                                (holder).description.setText(body_entity.getFor_consumer() + "");
                            }
                        } catch (Exception ignored) {

                        }
                    }
                }

            }
            break;
            default:
                break;
        }

        if (mIsFileBased) {
            String fileUrl = MPChatUtility.getFileUrlFromThread(thread) + "Medium.jpg";
            ImageUtils.loadImage((holder).fileThumbnail, fileUrl);
        } else {
            if (mThreadListInterface.getChatListType().equalsIgnoreCase(MPChatConstants.BUNDLE_VALUE_GROUP_CHAT_LIST)){
                List<String> imageList = getRecipientsImageList(thread);
                if (imageList != null && imageList.size()>0){
                    (holder).imageViewGroup.setImageDataList(imageList);
                }
            }else {
                if (MPChatUtility.isAvatarImageIsDefaultForUser(sender.profile_image))
                    (holder).userThumbnail.setImageResource(R.drawable.default_useravatar);
                else
                    ImageUtils.loadUserAvatar((holder).userThumbnail, sender.profile_image);
            }
        }
    }


    private String getDisplayName(MPChatThread thread) {
        MPChatUser sender = MPChatUtility.getComplimentryUserFromThread(thread, "" + mThreadListInterface.getLoggedInUserId());
        String userName = MPChatUtility.getUserDisplayNameFromUser(sender.name);
        LogUtils.i("sender_name", userName);
        String displayName = null;

        assert (userName != null && !userName.isEmpty());

        if (!isUserConsumer()) {
            userName = trimAndAddEllipsis(userName, kMaxNameLength);

            String assetName = MPChatUtility.getAssetNameFromThread(thread);

            if (assetName != null && !assetName.isEmpty()) {
                assetName = trimAndAddEllipsis(assetName, kMaxAssetNameLength);
                userName = trimAndAddEllipsis(userName, kMaxUserName);
                displayName = userName + "/" + assetName;
            } else
                displayName = userName;
        } else
            displayName = trimAndAddEllipsis(userName, kMaxTotalNameLength);

        return displayName;
    }


    private String trimAndAddEllipsis(String original, int maxCharacters) {
        if (original.length() > maxCharacters)
            return original.substring(0, maxCharacters) + "…";
        else
            return original;
    }

    /*
    * 获取群聊列表里参与人的头像url
    * */
    private List<String> getRecipientsImageList(MPChatThread thread){
        if (thread.recipients != null && thread.recipients.users.size()>0){
            List<String> imageList = new ArrayList<>();
            for (MPChatUser chatUser : thread.recipients.users){
                if (!TextUtils.isEmpty(chatUser.profile_image)){
                    imageList.add(chatUser.profile_image);
                }
            }
            return imageList;
        }
        return null;
    }

    /*
    * 获取群聊列表显示的项目名字
    * */
    private String getProjectName(MPChatThread thread){
        if (thread.entity != null && thread.entity.entityInfos.size()>0){
            MPChatEntityData entityData = thread.entity.entityInfos.get(0).entity_data;
            if (entityData !=null && !TextUtils.isEmpty(entityData.asset_name)){
                if (entityData.asset_name.equalsIgnoreCase("unbound")){
                    MPChatUser sender = MPChatUtility.getComplimentryUserFromThread(thread, "" + mThreadListInterface.getLoggedInUserId());
                    return MPChatUtility.getUserDisplayNameFromUser(sender.name);
                }
                return entityData.asset_name;
            }
        }
        return null;
    }

    private class ViewHolder {
        private RoundImageViewGroup imageViewGroup;
        private ImageView fileThumbnail;
        private CircleImageView userThumbnail;
        private TextView unreadMessageCount;
        private TextView name;
        private TextView description;
        private TextView time;
    }

    private static final int kMaxNameLength = 8;
    private static final int kMaxAssetNameLength = 4;
    private static final int kMaxUserName = 3;
    private static final int kMaxTotalNameLength = 8;

    private boolean mIsFileBased;
    private Context mContext;
    private ThreadListAdapterInterface mThreadListInterface;

}
