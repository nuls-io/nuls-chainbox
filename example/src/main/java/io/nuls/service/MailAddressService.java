package io.nuls.service;

import io.nuls.Config;
import io.nuls.controller.vo.MailAddressData;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.log.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 11:53
 * @Description: 功能描述
 */
@Service
public class MailAddressService {

    public static final String FILE_NAME = "address";

    public static final String SPLIT = ";";

    @Autowired
    Config config;

    public void saveMailAddress(MailAddressData mailAddressData) {
        synchronized (this){
            try {
                List<MailAddressData> data = getAllMailAddress();
                if(hasAddressOrMailAddress(data,mailAddressData.getAddress(),mailAddressData.getMailAddress())){
                    Log.error("address mail exists");
                    return ;
                }
                data.add(mailAddressData);
                saveMailAddressToFile(data);
            } catch (IOException e) {
                Log.error("save mail file fail",e);
            }

        }
    }

    public Optional<MailAddressData> getMailAddressPubKey(String mailAddress) throws IOException {
        return getAllMailAddress().stream().filter(d->d.getMailAddress().equals(mailAddress)).findFirst();
    }

    public Optional<MailAddressData> getMailAddress(String address) throws IOException {
        return getAllMailAddress().stream().filter(d->d.getAddress().equals(address)).findFirst();
    }

    public void removeMailAddress(String mailAddress) {
        synchronized (this){
            try {
                List<MailAddressData> data = getAllMailAddress().stream().filter(d->!d.getMailAddress().equals(mailAddress)).collect(Collectors.toList());
                saveMailAddressToFile(data);
            } catch (IOException e) {
                Log.error("save mail file fail",e);
            }

        }
    }

    public boolean hasAddressOrMailAddress(String address,String mailAddress) throws IOException {
        return hasAddressOrMailAddress(getAllMailAddress(),address,mailAddress);
    }

    public boolean hasAddressOrMailAddress(List<MailAddressData> allMailAddress,String address,String mailAddress) throws IOException {
        return allMailAddress.stream().anyMatch(mad->mad.getAddress().equals(address)||mad.getMailAddress().equals(mailAddress));
    }

    private List<MailAddressData> getAllMailAddress() throws IOException {
        List<MailAddressData> res = new ArrayList<>();
        File file = new File(getDataFile());
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null) {
            String[] data = line.split(SPLIT);
            MailAddressData mad = new MailAddressData();
            mad.setAddress(data[0]);
            mad.setMailAddress(data[1]);
            mad.setPubKey(data[2]);
            res.add(mad);
            line = reader.readLine();
        }
        reader.close();
        return res;
    }

    private void saveMailAddressToFile(List<MailAddressData> data) throws IOException {
        File file = new File(getDataFile());
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (MailAddressData d : data) {
            String item = new StringBuilder()
                    .append(d.getAddress()).append(SPLIT)
                    .append(d.getMailAddress()).append(SPLIT)
                    .append(d.getPubKey()).append(SPLIT).toString();
            writer.write(item);
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    private String getDataFile() {
        return config.getDataPath() + File.separator + FILE_NAME;
    }

}
