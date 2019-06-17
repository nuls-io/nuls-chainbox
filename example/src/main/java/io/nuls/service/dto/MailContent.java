package io.nuls.service.dto;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-13 17:57
 * @Description: 功能描述
 */
public class MailContent extends BaseNulsData {

    private byte[] receiverAddress;

    private byte[] senderAddress;

    private byte[] receiverKey;

    private byte[] senderKey;

    private byte[] title;

    private byte[] content;

    private Long timestamp;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(receiverAddress);
        stream.writeBytesWithLength(senderAddress);
        stream.writeBytesWithLength(receiverKey);
        stream.writeBytesWithLength(senderKey);
        stream.writeBytesWithLength(title);
        stream.writeBytesWithLength(content);
        stream.writeInt64(timestamp);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.receiverAddress = byteBuffer.readByLengthByte();
        this.senderAddress = byteBuffer.readByLengthByte();
        this.receiverKey = byteBuffer.readByLengthByte();
        this.senderKey = byteBuffer.readByLengthByte();
        this.title = byteBuffer.readByLengthByte();
        this.content = byteBuffer.readByLengthByte();
        this.timestamp = byteBuffer.readInt64();
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfBytes(receiverAddress);
        s += SerializeUtils.sizeOfBytes(receiverKey);
        s += SerializeUtils.sizeOfBytes(senderAddress);
        s += SerializeUtils.sizeOfBytes(senderKey);
        s += SerializeUtils.sizeOfBytes(title);
        s += SerializeUtils.sizeOfBytes(content);
        s += SerializeUtils.sizeOfInt64();
        return s;
    }

    public byte[] getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(byte[] receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public byte[] getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(byte[] senderAddress) {
        this.senderAddress = senderAddress;
    }

    public byte[] getReceiverKey() {
        return receiverKey;
    }

    public void setReceiverKey(byte[] receiverKey) {
        this.receiverKey = receiverKey;
    }

    public byte[] getSenderKey() {
        return senderKey;
    }

    public void setSenderKey(byte[] senderKey) {
        this.senderKey = senderKey;
    }

    public byte[] getTitle() {
        return title;
    }

    public void setTitle(byte[] title) {
        this.title = title;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"receiverAddress\":")
                .append(Arrays.toString(receiverAddress))
                .append(",\"senderAddress\":")
                .append(Arrays.toString(senderAddress))
                .append(",\"receiverKey\":")
                .append(Arrays.toString(receiverKey))
                .append(",\"senderKey\":")
                .append(Arrays.toString(senderKey))
                .append(",\"title\":")
                .append(Arrays.toString(title))
                .append(",\"content\":")
                .append(Arrays.toString(content))
                .append(",\"timestamp\":")
                .append(timestamp)
                .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MailContent)) return false;

        MailContent that = (MailContent) o;

        if (!Arrays.equals(receiverAddress, that.receiverAddress)) return false;
        if (!Arrays.equals(senderAddress, that.senderAddress)) return false;
        if (!Arrays.equals(receiverKey, that.receiverKey)) return false;
        if (!Arrays.equals(senderKey, that.senderKey)) return false;
        if (!Arrays.equals(title, that.title)) return false;
        if (!Arrays.equals(content, that.content)) return false;
        return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(receiverAddress);
        result = 31 * result + Arrays.hashCode(senderAddress);
        result = 31 * result + Arrays.hashCode(receiverKey);
        result = 31 * result + Arrays.hashCode(senderKey);
        result = 31 * result + Arrays.hashCode(title);
        result = 31 * result + Arrays.hashCode(content);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }
}
