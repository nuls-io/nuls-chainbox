package io.nuls.service.dto;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 17:08
 * @Description: 功能描述
 */
public class MailAddress extends BaseNulsData {

    private byte[] address;

    private byte[] pubKey;

    private String mailAddress;


    public byte[]  getAddress() {
        return address;
    }

    public void setAddress(byte[]  address) {
        this.address = address;
    }

    public byte[]  getPubKey() {
        return pubKey;
    }

    public void setPubKey(byte[]  pubKey) {
        this.pubKey = pubKey;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(address);
        stream.writeString(mailAddress);
        stream.writeBytesWithLength(pubKey);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        address = byteBuffer.readByLengthByte();
        mailAddress = byteBuffer.readString();
        pubKey = byteBuffer.readByLengthByte();
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfBytes(address);
        s += SerializeUtils.sizeOfString(mailAddress);
        s += SerializeUtils.sizeOfBytes(pubKey);
        return s;
    }
}
