package io.nuls.service;

import io.nuls.Config;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.controller.vo.MailAddressData;
import io.nuls.controller.vo.MailContentData;
import io.nuls.core.crypto.ECIESUtil;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.CryptoException;
import io.nuls.service.dto.MailContent;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 11:55
 * @Description: 功能描述
 */
@Service
public class SendMailService implements InitializingBean {

    public static final String SPLIT = ";";

    @Autowired
    Config config;

    @Autowired
    MailAddressService mailAddressService;

    /**
     * 保存一封邮件
     *
     * @return
     */
    public void saveMail(String hash, MailContent mailContent) throws IOException {
        //存储邮件内容到文件
        saveMailContent(hash, mailContent);
        //存储邮件与账户关系
        saveMailRelation(hash, mailContent);
    }

    private void saveMailRelation(String hash, MailContent mailContent) throws IOException {
        saveMailRelation(hash, mailContent, new File(getRecipientMappingPath() + File.separator + AddressTool.getStringAddressByBytes(mailContent.getReceiverAddress())));
        saveMailRelation(hash, mailContent, new File(getSenderMappingPath() + File.separator + AddressTool.getStringAddressByBytes(mailContent.getSenderAddress())));
    }

    private void saveMailRelation(String hash, MailContent mailContent, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(hash);
        writer.newLine();
        writer.flush();
        writer.close();
    }

    private void saveMailContent(String hash, MailContent mailContent) throws IOException {
        File file = new File(getMailData() + File.separator + hash);
        if (file.exists()) {
            throw new NulsRuntimeException(CommonCodeConstanst.DATA_ERROR, "save mail fail , mail content file exists");
        }
        File folder = new File(file.getParent());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        BufferedWriter outputStream = new BufferedWriter(new FileWriter(file));
        outputStream.write(HexUtil.encode(mailContent.serialize()));
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 获取邮件内容
     */
    public MailContentData getMailContent(String hash, ECKey key, byte[] address) throws IOException, NulsException, CryptoException {
        File file = new File(getMailData() + File.separator + hash);
        if (!file.exists()) {
            throw new NulsRuntimeException(CommonCodeConstanst.DATA_ERROR, "error mail hash");
        }
        MailContentData mcd = new MailContentData();
        BufferedReader inputStream = new BufferedReader(new FileReader(file));
        String tmp = inputStream.readLine();
        MailContent mailContent = new MailContent();
        mailContent.parse(new NulsByteBuffer(HexUtil.decode(tmp)));
        inputStream.close();
        String readKey;
        if (Arrays.equals(address, mailContent.getSenderAddress())) {
            readKey = HexUtil.encode(mailContent.getSenderKey());
        } else {
            readKey = HexUtil.encode(mailContent.getReceiverKey());
        }
        byte[] priKey = ECIESUtil.decrypt(key.getPrivKeyBytes(), readKey);
        byte[] title = ECIESUtil.decrypt(priKey, HexUtil.encode(mailContent.getTitle()));
        mcd.setTitle(new String(title, StandardCharsets.UTF_8));
        byte[] content = ECIESUtil.decrypt(priKey, HexUtil.encode(mailContent.getContent()));
        mcd.setContent(new String(content, StandardCharsets.UTF_8));
        mcd.setSender(AddressTool.getStringAddressByBytes(mailContent.getSenderAddress()));
        MailAddressData receiverMailAddressData = mailAddressService.getMailAddress(AddressTool.getStringAddressByBytes(mailContent.getReceiverAddress())).get();
        mcd.setReceiverMailAddress(receiverMailAddressData.getMailAddress());
        MailAddressData senderMailAddress = mailAddressService.getMailAddress(AddressTool.getStringAddressByBytes(mailContent.getSenderAddress())).get();
        mcd.setSenderMailAddress(senderMailAddress.getMailAddress());
        mcd.setDate(new Date(mailContent.getTimestamp()));
        return mcd;
    }

    public List<MailContentData> getMailList(String address, ECKey ecKey, boolean isSender) throws IOException, CryptoException, NulsException {
        File file = new File((isSender ? getSenderMappingPath() : getRecipientMappingPath()) + File.separator + address);
        if (!file.exists()) {
            return List.of();
        }
        List<MailContentData> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String hash = reader.readLine();
            while (hash != null) {
                MailContentData mailContentData = getMailContent(hash, ecKey, AddressTool.getAddress(address));
                mailContentData.setHash(hash);
                list.add(mailContentData);
                hash = reader.readLine();
            }
        }
        return list;
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        //初始化数据存储文件夹
        File file = new File(config.getDataPath());
        if (!file.exists()) {
            file.mkdir();
        }
        createDir(getMailData());
        createDir(getRecipientMappingPath());
        createDir(getSenderMappingPath());
    }

    public String getSenderMappingPath() {
        return config.getDataPath() + File.separator + "sender";
    }

    public String getRecipientMappingPath() {
        return config.getDataPath() + File.separator + "recipient";
    }

    public String getMailData() {
        return config.getDataPath() + File.separator + "mail-data";
    }

    private File createDir(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

}
