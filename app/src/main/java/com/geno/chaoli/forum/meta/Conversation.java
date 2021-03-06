package com.geno.chaoli.forum.meta;

import android.graphics.drawable.Drawable;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.StringTokenizer;

public class Conversation
{
	public int conversationId;
	public Channel channel;
	public String title;
	public String excerpt;
	public String link;

	public String startMemberId;

	public String lastPostMemberId;
	public String startMember;
	@JSONField(name="startMemberAvatarFormat")
	public String startMemberAvatarSuffix;

	@JSONField(name="lastPostMemberAvatarFormat")
	public String lastPostMemberAvatarSuffix;

	public Drawable startAvatar;
	public String lastPostMember;
	public Drawable lastPostAvatar;
	public String lastPostTime;
	public int replies;
	public List<ConversationState> state;
	public int getConversationId()
	{
		return conversationId;
	}
	public void setConversationId(int conversationId)
	{
		this.conversationId = conversationId;
	}

	public Channel getChannel()
	{
		return channel;
	}

	public void setChannel(Channel channel)
	{
		this.channel = channel;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getExcerpt()
	{
		return excerpt;
	}

	public void setExcerpt(String excerpt)
	{
		this.excerpt = excerpt;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}

	public String getStartMemberId() {
		return startMemberId;
	}

	public void setStartMemberId(String startMemberId) {
		this.startMemberId = startMemberId;
	}

	public String getLastPostMemberId() {
		return lastPostMemberId;
	}

	public void setLastPostMemberId(String lastPostMemberId) {
		this.lastPostMemberId = lastPostMemberId;
	}

	public String getStartMember()
	{
		return startMember;
	}

	public void setStartMember(String startMember)
	{
		this.startMember = startMember;
	}

	public Drawable getStartAvatar()
	{
		return startAvatar;
	}

	public void setStartAvatar(Drawable startAvatar)
	{
		this.startAvatar = startAvatar;
	}

	public String getLastPostMember()
	{
		return lastPostMember;
	}

	public void setLastPostMember(String lastPostMember)
	{
		this.lastPostMember = lastPostMember;
	}

	public Drawable getLastPostAvatar()
	{
		return lastPostAvatar;
	}

	public void setLastPostAvatar(Drawable lastPostAvatar)
	{
		this.lastPostAvatar = lastPostAvatar;
	}

	public String getStartMemberAvatarSuffix() {
		return startMemberAvatarSuffix;
	}

	public void setStartMemberAvatarSuffix(String startMemberAvatarSuffix) {
		this.startMemberAvatarSuffix = startMemberAvatarSuffix;
	}

	public String getLastPostMemberAvatarSuffix() {
		return lastPostMemberAvatarSuffix;
	}

	public void setLastPostMemberAvatarSuffix(String lastPostMemberAvatarSuffix) {
		this.lastPostMemberAvatarSuffix = lastPostMemberAvatarSuffix;
	}

	public String getLastPostTime()
	{
		return lastPostTime;
	}

	public void setLastPostTime(String lastPostTime)
	{
		this.lastPostTime = lastPostTime;
	}

	public int getReplies()
	{
		return replies;
	}

	public void setReplies(int replies)
	{
		this.replies = replies;
	}

	public List<ConversationState> getState()
	{
		return state;
	}

	public void setState(List<ConversationState> state)
	{
		this.state = state;
	}
}
