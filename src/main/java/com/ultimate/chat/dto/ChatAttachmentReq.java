package com.ultimate.chat.dto;

@lombok.Data
public class ChatAttachmentReq {

	private String fileId;
	private String fileName;
	private String fileType;
	private long fileSize;
	private String downloadUrl;
	private String storagePath;
	private short encrypted;
	private String iv;
	private String fileData;

	public ChatAttachmentReq() {
	}

	public ChatAttachmentReq(String fileId, String fileName, String fileType, long fileSize, String downloadUrl) {

		this.fileId = fileId;
		this.fileName = fileName;
		this.fileType = fileType;
		this.fileSize = fileSize;
		this.downloadUrl = downloadUrl;
	}

	public ChatAttachmentReq(String fileId, String fileName, String fileType, long fileSize, String downloadUrl, String storagePath, short encrypted, String iv) {
		this.fileId = fileId;
		this.fileName = fileName;
		this.fileType = fileType;
		this.fileSize = fileSize;
		this.downloadUrl = downloadUrl;
		this.storagePath = storagePath;
		this.encrypted = encrypted;
		this.iv = iv;
	}

}
