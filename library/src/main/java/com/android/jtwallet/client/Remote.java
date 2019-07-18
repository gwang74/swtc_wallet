package com.android.jtwallet.client;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.jtwallet.client.bean.Account;
import com.android.jtwallet.client.bean.AccountData;
import com.android.jtwallet.client.bean.AccountInfo;
import com.android.jtwallet.client.bean.AccountTums;
import com.android.jtwallet.client.bean.AccountTx;
import com.android.jtwallet.client.bean.AmountInfo;
import com.android.jtwallet.client.bean.Ledger;
import com.android.jtwallet.client.bean.LedgerClosed;
import com.android.jtwallet.client.bean.LedgerInfo;
import com.android.jtwallet.client.bean.Marker;
import com.android.jtwallet.client.bean.Memo;
import com.android.jtwallet.client.bean.ServerInfo;
import com.android.jtwallet.client.bean.Transactions;
import com.android.jtwallet.config.Config;
import com.android.jtwallet.connection.Connection;
import com.android.jtwallet.exceptions.RemoteException;
import com.android.jtwallet.listener.Impl.LedgerCloseImpl;
import com.android.jtwallet.listener.Impl.TransactionsImpl;
import com.android.jtwallet.listener.RemoteInter;
import com.android.jtwallet.utils.CheckUtils;
import com.android.jtwallet.utils.JsonUtils;
import com.android.jtwallet.utils.Utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Remote {
    protected static final Logger logger = LoggerFactory.getLogger(Remote.class);
    private String server = "";
    // 签名默认为false,false需要传入密钥
    private Boolean localSign = false;
    private Connection conn = null;

    protected ScheduledExecutorService service;
    // All code that use the Client api, must be run on this thread

    protected Thread clientThread;

    public Remote(Connection conn) {
        this.conn = conn;
        this.initClientThread();
        this.initClientThread();
    }

    public Remote(Connection conn, Boolean localSign) {
        this.conn = conn;
        this.localSign = localSign;
        this.initClientThread();

    }

    public void initClientThread() {
        service = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                clientThread = new Thread(r);
                return clientThread;
            }
        });
    }

    /**
     * 工具方法：组装发送前的信息
     *
     * @param command
     * @param data
     * @return
     */
    public String sendMessage(String command, Map<String, Object> data) {
        Map<String, Object> params = new HashMap();
        params.put("command", command);
        params.putAll(data);
        logger.debug("WebSocket参数： " + JsonUtils.toJsonString(params));
        return conn.submit(params);
    }

    // 4.1 创建Remote对象

    /**
     * 4.2 创建连接 获取服务信息及帐本信息
     *
     * @return
     */
    public LedgerInfo requestLedgerInfo() {
        Map<String, Object> params = new HashMap();
//		params.put("streams", new String[] { "ledger", "server" });
        //订阅最新交易信息
        params.put("streams", new String[]{"transactions"});
        String res = sendMessage("subscribe", params);
        return JsonUtils.toEntity(res, LedgerInfo.class);
    }

    /**
     * 4.4 请求底层服务器信息
     *
     * @return
     */
    public ServerInfo requestServerInfo() {
        ServerInfo ser = new ServerInfo();
        Map params = new HashMap();
        String msg = this.sendMessage("server_info", params);
        return JsonUtils.toEntity(msg, ServerInfo.class);
    }

    /**
     * 4.5 获取最新账本信息
     *
     * @return
     */
    public LedgerClosed requestLedgerClosed() {
        Map params = new HashMap();
        String msg = this.sendMessage("ledger_closed", params);
        return JsonUtils.toEntity(msg, LedgerClosed.class);
    }

    /**
     * 4.6 获取某一账本具体信息
     * 注：整体参数是Object类型，当参数都不填时，默认返回最新账本信息
     *
     * @param ledgerIndexOrHash 井通区块高度||井通区块hash(与上面ledger_index二选其一)
     * @param transactions      是否返回账本上的交易记录hash，默认false
     * @return
     */
    public Ledger requestLedger(String ledgerIndexOrHash, boolean transactions) {
        Map params = new HashMap();
        // 校验,并将参数写入message对象
        Map message = new HashMap();
        if (StringUtils.isEmpty(ledgerIndexOrHash)) {
            throw new RemoteException("ledger_index and ledger_hash is null");
        }
        if (StringUtils.isBlank(ledgerIndexOrHash)) {
            throw new RemoteException("invalid ledger_index or ledger_hash is null");
        }
        if (CheckUtils.isNumeric(ledgerIndexOrHash)) {
            params.put("ledger_index", Integer.parseInt(ledgerIndexOrHash));
        } else {
            params.put("ledger_hash", ledgerIndexOrHash);
        }
        params.put("transactions", transactions);
        String msg = this.sendMessage("ledger", params);
        return JsonUtils.toEntity(msg, Ledger.class);
    }

    /**
     * 4.7 查询某一交易具体信息
     *
     * @param hash 交易hash
     * @return
     */
    public Account requestTx(String hash) {
        Map params = new HashMap();
        // 校验,并将参数写入message对象
        if (!TextUtils.isEmpty(hash)) {
            throw new RemoteException("invalid tx hash");
        }
        params.put("transaction", hash);
        String msg = this.sendMessage("tx", params);
        Map map = JsonUtils.toObject(msg, Map.class);
        Account account = new Account();
        if (map.get("status").equals("success")) {
            JSONObject msgJson = JSONObject.parseObject(msg);
            JSONObject resultJson = JSONObject.parseObject(msgJson.getString("result"));
            if (StringUtils.isNotBlank(resultJson.getString("Amount"))) {
                String type = resultJson.get("Amount").getClass().toString();
                if (!type.equals(JSONObject.class.toString())) {
                    JSONObject amountJson = new JSONObject();
                    amountJson.put("currency", Config.CURRENCY);
                    amountJson.put("value", Utils.amountFormatDivide(resultJson.get("Amount").toString()));
                    amountJson.put("issuer", "");
                    resultJson.put("Amount", amountJson);
                    msgJson.put("result", resultJson);
                    msg = msgJson.toJSONString();
                }
            }
            if (StringUtils.isNotBlank(resultJson.getString("TakerGets"))) {
                String type = resultJson.get("TakerGets").getClass().toString();
                if (!type.equals(JSONObject.class.toString())) {
                    JSONObject amountJson = new JSONObject();
                    amountJson.put("currency", Config.CURRENCY);
                    amountJson.put("value", Utils.amountFormatDivide(resultJson.get("TakerGets").toString()));
                    amountJson.put("issuer", "");
                    resultJson.put("TakerGets", amountJson);
                    msgJson.put("result", resultJson);
                    msg = msgJson.toJSONString();
                }
            }
            if (StringUtils.isNotBlank(resultJson.getString("TakerPays"))) {
                String type = resultJson.get("TakerPays").getClass().toString();
                if (!type.equals(JSONObject.class.toString())) {
                    JSONObject amountJson = new JSONObject();
                    amountJson.put("currency", Config.CURRENCY);
                    amountJson.put("value", Utils.amountFormatDivide(resultJson.get("TakerPays").toString()));
                    amountJson.put("issuer", "");
                    resultJson.put("TakerPays", amountJson);
                    msgJson.put("result", resultJson);
                    msg = msgJson.toJSONString();
                }
            }
            account = JsonUtils.toEntity(msg, Account.class);
        } else if (map.get("status").equals("error")) {
            msg = "接口调用出错";
            throw new RuntimeException(msg);
        } else {
            throw new RuntimeException("unknown error");
        }
        return account;
    }

    /**
     * 4.8 请求账号信息
     *
     * @param account 井通钱包地址
     * @return
     */
    public AccountInfo requestAccountInfo(String account, Object ledger) {
        String msg = requestAccount("account_info", account, ledger, "");
        AccountInfo accountInfo = JsonUtils.toEntity(msg, AccountInfo.class);
        if (accountInfo != null) {
            AccountData accountData = accountInfo.getAccountData();
            accountData.setBalance(Utils.amountFormatDivide(accountData.getBalance()));
            accountInfo.setAccountData(accountData);
        }
        return accountInfo;
    }

    public AccountInfo requestAccountInfo(String account, Object ledger, String type) {
        String msg = requestAccount("account_info", account, ledger, type);
        AccountInfo accountInfo = JsonUtils.toEntity(msg, AccountInfo.class);
        if (accountInfo != null) {
            AccountData accountData = accountInfo.getAccountData();
            accountData.setBalance(Utils.amountFormatDivide(accountData.getBalance()));
            accountInfo.setAccountData(accountData);
        }
        return accountInfo;
    }

    /**
     * 4.9 获得账号可接收和发送的货币
     *
     * @param account 井通钱包地址
     * @return
     */
    public AccountTums requestAccountTums(String account, Object ledger) {
        String msg = requestAccount("account_currencies", account, ledger, "");
        AccountTums accountTums = JsonUtils.toEntity(msg, AccountTums.class);
        return accountTums;
    }

    /**
     * 获取账号信息
     * inner function
     *
     * @param command
     * @param account
     * @param type
     * @return
     */
    private String requestAccount(String command, String account, Object ledger, String type) {
        Map params = new HashMap();
        // 校验,并将参数写入message对象
        if (StringUtils.isNotEmpty(type)) {
            Integer relation_type = null;
            switch (type) {
                case "trust":
                    relation_type = 0;
                    break;
                case "authorize":
                    relation_type = 1;
                    break;
                case "freeze":
                    relation_type = 3;
                    break;
            }
            params.put("relation_type", relation_type);
        }
        if (!CheckUtils.isValidAddress(account)) {
            throw new RemoteException("invalid account");
        } else {
            params.put("account", account);
        }

        // params.put("message", message);
        params.put("account", account);
        params.put("command", command);
        if (ledger != null) {
            Request request = new Request();
            Map map = request.selectLedger(ledger);
            params.putAll(map);
        }

        String msg = this.submit(params);
        return msg;
    }

    /**
     * 获得账号交易列表
     *
     * @param account 井通钱包地址
     * @param limit   限定返回多少条记录，默认200
     * @return
     */
    public AccountTx requestAccountTx(String account, Integer limit, Marker marker) {
        if (limit == null || limit == 0) {
            limit = 200;
        }
        Map params = new HashMap();
        // 校验,并将参数写入message对象
        if (!CheckUtils.isValidAddress(account)) {
            throw new RemoteException("account parameter is invalid");
        } else {
            params.put("account", account);
        }
        if (limit != null) {
            params.put("limit", limit);
        }
        params.put("account", account);
        params.put("command", "account_tx");
        // 新增参数start
        params.put("ledger_index_min", 0);
        params.put("ledger_index_max", -1);
        if (marker != null) {
            params.put("marker", JSONObject.toJSON(marker));
        }
        // 新增参数end
        String msg = this.submit(params);
        AccountTx accountTx = new AccountTx();
        JSONObject json = JSONObject.parseObject(msg);
        if (json.get("status").equals("success")) {
            JSONObject result = JSONObject.parseObject(json.get("result").toString());
            accountTx.setAccount(result.get("account").toString());
            accountTx.setLedgerIndexMax(result.get("ledger_index_max").toString());
            accountTx.setLedgerIndexMin(result.get("ledger_index_min").toString());
            if (result.get("limit") != null) {
                accountTx.setLimit(result.getInteger("limit"));
            }
            if (result.get("marker") != null) {
                Marker markerResult = new Marker();
                markerResult.setLedger(JSONObject.parseObject(result.get("marker").toString()).getInteger("ledger"));
                markerResult.setSeq(JSONObject.parseObject(result.get("marker").toString()).getInteger("seq"));
                accountTx.setMarker(markerResult);
            }
            JSONArray list = JSONArray.parseArray(result.get("transactions").toString());
            List<Transactions> txs = new ArrayList<>();
            for (Object txObject : list) {
                Transactions transactions = processTx((JSONObject) txObject, account);
                txs.add(transactions);
            }
            accountTx.setTransactions(txs);
        }

        return accountTx;
    }

    public Transactions processTx(JSONObject txJsonObject, String account) {
        Transactions transtacion = new Transactions();
        JSONObject tx = new JSONObject();
        JSONObject meta = new JSONObject();
        if (txJsonObject.get("tx") != null) {
            tx = (JSONObject) txJsonObject.get("tx");
        } else if ((JSONObject) txJsonObject.get("transaction") != null) {
            tx = (JSONObject) txJsonObject.get("transaction");
        } else {
            tx = (JSONObject) txJsonObject;
        }
        meta = (JSONObject) txJsonObject.get("meta");
        String hex = "0x386D4380";
        Integer x = Integer.parseInt(hex.substring(2), 16);
        if (tx.get("date") != null) {
            transtacion.setDate(Integer.valueOf(tx.get("date").toString()) + x);
        } else {
            transtacion.setDate(Integer.valueOf(tx.get("Timestamp").toString()) + x);
        }
        if (tx.get("hash") != null) {
            transtacion.setHash(tx.get("hash").toString());
        }
        transtacion.setType(txnType(tx, account));
        transtacion.setFee(Utils.amountFormatDivide(tx.get("Fee").toString()));

        if (meta.get("TransactionResult") != null) {
            transtacion.setResult(meta.get("TransactionResult").toString());
        } else {
            transtacion.setResult("failed");
        }

        switch (transtacion.getType()) {
            case "sent":
                transtacion.setCounterparty(tx.get("Destination").toString());
                transtacion.setAmount(parseAmount(tx.get("Amount")));
                break;
            case "received":
                transtacion.setCounterparty(tx.get("Account").toString());
                transtacion.setAmount(parseAmount(tx.get("Amount")));
                break;
            case "trusted":
                transtacion.setCounterparty(tx.get("Account").toString());
                transtacion.setAmount(reverseAmount(JSONObject.parseObject(tx.get("LimitAmount").toString()), tx.get("Account").toString()));
                break;
            case "trusting":
                transtacion.setCounterparty(JSONObject.parseObject(tx.get("LimitAmount").toString()).get("issuer").toString());
                transtacion.setAmount(parseAmount(tx.get("LimitAmount")));
                break;
            case "convert":
                transtacion.setSpent(parseAmount(tx.get("SendMax")));
                transtacion.setAmount(parseAmount(tx.get("Account")));
                break;
            case "offernew":
                //Transaction.flags.OfferCreate.Sell 是一个常量
                transtacion.setOffertype((tx.get("Flags") != null ? "sell" : "buy"));
                transtacion.setGets(parseAmount(tx.get("TakerGets")));
                transtacion.setPays(parseAmount(tx.get("TakerPays")));
                transtacion.setSeq(Integer.valueOf(tx.get("Sequence").toString()));
                break;
            case "offercancel":
                transtacion.setOfferseq(Integer.valueOf(tx.get("Sequence").toString()));
                break;
            case "relationset":
                transtacion.setCounterparty(account.equals(tx.get("Target").toString()) ? tx.get("Account").toString() : tx.get("Target").toString());
                transtacion.setRelationtype(tx.get("RelationType").toString().equals("3") ? "freeze" : "authorize");
                transtacion.setIsactive(account.equals(tx.get("Target").toString()) ? false : true);
                transtacion.setAmount(parseAmount(tx.get("LimitAmount")));
                break;
            case "relationdel":
                transtacion.setCounterparty(account.equals(tx.get("Target").toString()) ? tx.get("Account").toString() : tx.get("Target").toString());
                transtacion.setRelationtype(tx.get("RelationType").toString().equals("3") ? "unfreeze" : "unknown");
                transtacion.setIsactive(account.equals(tx.get("Target").toString()) ? false : true);
                transtacion.setAmount(parseAmount(tx.get("LimitAmount")));
                break;
            case "configcontract":
                transtacion.setParams(formatArgs(JSONArray.parseArray(tx.get("Args").toString())));
                if (tx.get("Method").equals("0")) {
                    transtacion.setMethod("deploy");
                    transtacion.setPayload(tx.get("Payload").toString());

                } else if (tx.get("Method").equals("1")) {
                    transtacion.setMethod("call");
                    transtacion.setPayload(tx.get("Destination").toString());
                }
                break;
            default:
                // TODO parse other type
                break;
        }
        transtacion.setMemos(new ArrayList<Memo>());
        if (tx.get("Memos") != null && tx.get("Memos") instanceof JSONArray && ((List) tx.get("Memos")).size() > 0) {
            List menos = (List) tx.get("Memos");
            List<Memo> memosList = new ArrayList<Memo>();
            for (int i = 0; i < menos.size(); i++) {
                JSONObject jsonMemo = JSONObject.parseObject(menos.get(i).toString()).getJSONObject("Memo");
                Memo memo = new Memo();
                if (jsonMemo.get("MemoData") != null) {
                    memo.setMemoData(jsonMemo.get("MemoData").toString());
                }
                if (jsonMemo.get("MemoType") != null) {
                    memo.setMemoType(jsonMemo.get("MemoType").toString());
                }
                memosList.add(memo);
            }
            transtacion.setMemos(memosList);
        }

        transtacion.setEffects(new JSONArray());
        if (meta == null || !meta.get("TransactionResult").toString().equals("tesSUCCESS")) {
            return transtacion;
        }
        JSONArray array = JSONArray.parseArray(meta.get("AffectedNodes").toString());
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            JSONObject node = processAffectNode(object);
            JSONObject effect = new JSONObject();
            if (node.get("entryType").equals("Offer")) {
                JSONObject fields = node.getJSONObject("fields");
                JSONObject fieldsPrev = node.getJSONObject("fieldsPrev");
                JSONObject fieldsFinal = node.getJSONObject("fieldsFinal");
                boolean sell = fields.get("Flags") != null ? true : false;
                if (fields.get("Account").equals(account)) {
                    if ("ModifiedNode".equals(node.get("diffType")) ||
                            ("DeletedNode".equals(node.get("diffType")) && fieldsPrev != null && fieldsFinal != null && (fieldsPrev.get("TakerGets") != null &&
                                    !isAmountZero(parseAmount(fieldsFinal.get("TakerGets")))))) {

                        effect.put("effect", "offer_partially_funded");
                        JSONObject _json = new JSONObject();
                        _json.put("account", tx.get("Account"));
                        _json.put("seq", tx.get("Sequence"));
                        _json.put("hash", tx.get("hash"));
                        effect.put("counterparty", _json);
                        if (node.get("diffType").equals("DeletedNode")) {
                            if (isAmountZero(parseAmount(fields.get("TakerGets")))) {
                                effect.put("remaining", false);
                            } else {
                                effect.put("remaining", true);
                            }
                        } else {
                            effect.put("cancelled", true);
                        }
                        effect.put("gets", parseAmount(fields.get("TakerGets")));
                        effect.put("pays", parseAmount(fields.get("TakerPays")));
                        if (parseAmount(fieldsPrev.get("TakerGets")) != null) {
                            effect.put("paid", amountSubtract(
                                    parseAmount(fieldsPrev.get("TakerGets")),
                                    parseAmount(fields.get("TakerGets"))));
                        }

                        if (parseAmount(fieldsPrev.get("TakerGets")) != null) {
                            effect.put("got", amountSubtract(parseAmount(fieldsPrev.get("TakerPays")),
                                    parseAmount(fields.get("TakerPays"))));
                        }


                        effect.put("type", sell ? "sold" : "bought");

                    } else {
                        effect.put("effect", "CreatedNode".equals(node.get("diffType")) ? "offer_created" : fieldsPrev != null && fieldsPrev.get("TakerPays") != null ? "offer_funded" : "offer_cancelled");

                        if (effect.get("effect").equals("offer_funded")) {
                            fields = fieldsPrev;
                            JSONObject _object = new JSONObject();
                            _object.put("account", tx.get("Account"));
                            _object.put("seq", tx.get("Sequence"));
                            _object.put("hash", tx.get("hash"));
                            effect.put("counterparty", _object);
                            effect.put("paid", amountSubtract(
                                    parseAmount(JSONObject.parseObject(node.get("fieldsPrev").toString()).get("TakerGets")),
                                    parseAmount(JSONObject.parseObject(node.get("fields").toString()).get("TakerGets"))));

                            effect.put("got", amountSubtract(
                                    parseAmount(JSONObject.parseObject(node.get("fieldsPrev").toString()).get("TakerPays")),
                                    parseAmount(JSONObject.parseObject(node.get("fields").toString()).get("TakerPays"))));

                            effect.put("type", sell ? "sold" : "bought");
                        }
                        // 3. offer_created
                        if (effect.get("effect").equals("offer_created")) {
                            effect.put("gets", parseAmount(fields.get("TakerGets")));
                            effect.put("pays", parseAmount(fields.get("TakerPays")));
                            effect.put("type", sell ? "sell" : "buy");
                        }
                        // 4. offer_cancelled
                        if (effect.get("effect").equals("offer_cancelled")) {

                            effect.put("hash", fields.get("PreviousTxnID"));
                            // collect data for cancel transaction type
                            if (transtacion.getType().equals("offercancel")) {
                                transtacion.setGets(parseAmount(fields.get("TakerGets")));
                                transtacion.setPays(parseAmount(fields.get("TakerPays")));
                            }
                            effect.put("gets", parseAmount(fields.get("TakerGets")));
                            effect.put("pays", parseAmount(fields.get("TakerPays")));
                            effect.put("type", sell ? "sell" : "buy");
                        }

                    }
                    effect.put("seq", JSONObject.parseObject(node.get("fields").toString()).get("Sequence"));
                } else if (tx.get("Account").equals(account) && fieldsPrev != null) {
                    effect.put("effect", "offer_bought");
                    JSONObject _object = new JSONObject();
                    _object.put("account", JSONObject.parseObject(node.get("fields").toString()).get("Account"));
                    _object.put("seq", JSONObject.parseObject(node.get("fields").toString()).get("Sequence"));
                    if (node.get("PreviousTxnID") != null) {
                        _object.put("hash", JSONObject.parseObject(node.get("fields").toString()).get("PreviousTxnID"));
                    } else {
                        _object.put("hash", JSONObject.parseObject(node.get("fields").toString()).get("PreviousTxnID"));
                    }
                    effect.put("counterparty", _object);
                    effect.put("type", sell ? "bought" : "sold");
                    effect.put("paid", amountSubtract(
                            parseAmount(fieldsPrev.get("TakerPays")),
                            parseAmount(JSONObject.parseObject(node.get("fields").toString()).get("TakerPays"))));
                    effect.put("got", amountSubtract(
                            parseAmount(fieldsPrev.get("TakerGets")),
                            parseAmount(JSONObject.parseObject(node.get("fields").toString()).get("TakerGets"))));
                }
                // add price
                if ((effect.get("gets") != null && effect.get("pays") != null) || (effect.get("got") != null && effect.get("paid") != null)) {
                    Boolean created = effect.get("effect").toString().equals("offer_created") && effect.get("type").toString().equals("buy");
                    Boolean funded = effect.get("effect").toString().equals("offer_funded") && effect.get("type").toString().equals("bought");
                    Boolean cancelled = effect.get("effect").toString().equals("offer_cancelled") && effect.get("type").toString().equals("buy");
                    Boolean bought = effect.get("effect").toString().equals("offer_bought") && effect.get("type").toString().equals("bought");
                    Boolean partially_funded = effect.get("effect").toString().equals("offer_partially_funded") && effect.get("type").toString().equals("bought");
                    effect.put("price", getPrice(effect, (created || funded || cancelled || bought || partially_funded)));


                }
            }
            if (transtacion.getType().equals("offereffect") && node.get("entryType").equals("AccountRoot")) {
                JSONObject fields = node.getJSONObject("fields");
                if (fields.get("RegularKey") != null && fields.get("RegularKey").equals(account)) {
                    effect.put("effect", "set_regular_key");
                    effect.put("type", "null");
                    effect.put("account", fields.get("Account"));
                    effect.put("regularkey", account);
                }
            }
            // add effect
            if (effect != null && !effect.isEmpty()) {
                if (node.get("diffType").equals("DeletedNode") && effect.get("effect").equals("offer_bought")) {
                    effect.put("deleted", true);
                }
                transtacion.getEffects().add(effect);
            }
        }


        return transtacion;
    }

    public Boolean isAmountZero(AmountInfo amount) {
        if (StringUtils.isBlank(amount.getValue())) {
            return false;
        }
        return Integer.valueOf(amount.getValue()) < 1e-12;
    }

    public String getPrice(JSONObject effect, boolean funded) {
        AmountInfo g = new AmountInfo();
        AmountInfo p = new AmountInfo();
        if (effect.get("got") != null && effect.get("got") != "") {
            g = parseAmount(effect.get("got"));
        } else if (effect.get("pays") != null && effect.get("pays") != "") {
            g = parseAmount(effect.get("pays"));
        } else {
            return "";
        }

        if (effect.get("paid") != null && effect.get("paid") != "") {
            p = parseAmount(effect.get("paid"));
        } else if (effect.get("gets") != null && effect.get("gets") != "") {
            p = parseAmount(effect.get("gets"));
        } else {
            return "";
        }
        if (!funded) {
            return amountRatio(g, p);
        } else {
            return amountRatio(p, g);
        }

    }

    public String amountRatio(AmountInfo amount1, AmountInfo amount2) {
        if (amount1 != null && amount2 != null && "0".equals(amount1.getValue()) && "0".equals(amount2.getValue())) {
            BigDecimal bi1 = new BigDecimal(amount1.getValue());
            BigDecimal bi2 = new BigDecimal(amount2.getValue());
            BigDecimal bi3 = bi1.divide(bi2, 6, BigDecimal.ROUND_HALF_UP);
            return String.valueOf(bi3.doubleValue());
        } else {
            return "";
        }
    }

    public AmountInfo amountSubtract(AmountInfo amount1, AmountInfo amount2) {
        if (amount1 != null && amount2 != null) {
            try {
                return amountAdd(amount1, amount2);
            } catch (Exception e) {
                throw new RemoteException("to map error");
            }
        }
        return null;
    }

    public Map amountNegate(Map amount) {
        if (amount == null) {
            return amount;
        }
        Map map = new HashMap();
        map.put("value", (new BigInteger(amount.get("value").toString())).negate());
        map.put("currency", amount.get("currency"));
        map.put("issuer", amount.get("issuer"));
        return map;
    }

    public AmountInfo amountAdd(AmountInfo amount1, AmountInfo amount2) {
        if (amount1 == null) {
            return amount2;
        }
        if (amount2 == null) {
            return amount1;
        }
        if (amount1 != null && amount2 != null) {
            BigDecimal amountBg1 = new BigDecimal(amount1.getValue());
            BigDecimal amountBg2 = new BigDecimal(amount2.getValue());
            amount1.setValue(String.valueOf(amountBg1.add(amountBg2).intValue()));
            return amount1;
        }
        return null;
    }

    public JSONObject processAffectNode(JSONObject object) {
        JSONObject result = new JSONObject();
        String[] arrays = new String[]{"CreatedNode", "ModifiedNode", "DeletedNode"};
        for (int i = 0; i < arrays.length; i++) {
            if (object.get(arrays[i]) != null) {
                result.put("diffType", arrays[i]);
            }
        }
        if (result.get("diffType") == null) {
            return null;
        }
        object = object.getJSONObject(result.get("diffType").toString());
        result.put("entryType", object.get("LedgerEntryType"));
        result.put("ledgerIndex", object.get("LedgerIndex"));
        JSONObject _object = new JSONObject();
        if (object.get("PreviousFields") != null) {
            result.put("fieldsPrev", object.get("PreviousFields"));
            _object.putAll(object.getJSONObject("PreviousFields"));
        }
        if (object.get("NewFields") != null) {
            result.put("fieldsNew", object.get("NewFields"));
            _object.putAll(object.getJSONObject("NewFields"));
        }
        if (object.get("FinalFields") != null) {
            result.put("fieldsFinal", object.get("FinalFields"));
            _object.putAll(object.getJSONObject("FinalFields"));
        }
        if (object.get("PreviousTxnID") != null) {
            result.put("PreviousTxnID", object.get("PreviousTxnID"));
        }
        result.put("fields", _object);
        return result;
    }

    public List formatArgs(JSONArray args) {
        List list = new ArrayList();
        if (args != null) {
            for (int i = 0; i < args.size(); i++) {
                JSONObject jo = (JSONObject) args.get(i);
                list.add(hexToString(JSONObject.parseObject(jo.get("Arg").toString()).get("Parameter").toString()));
            }
        }
        return list;
    }

    public String hexToString(String str) {
        List<String> list = new ArrayList<String>();
        int i = 0;
        if (str.length() % 2 == 0) {
            list.add(unicode2String(String.valueOf(Integer.parseInt(str.substring(0, 1), 16))));
            i = 1;
        }
        for (; i < str.length(); i += 2) {
            list.add(unicode2String(String.valueOf(Integer.parseInt(str.substring(i, i + 2), 16))));
        }
        return TextUtils.join("", list);
    }

    public static String unicode2String(String unicode) {

        StringBuffer string = new StringBuffer();
        String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            // 转换出每一个代码点
            int data = Integer.parseInt(hex[i], 16);
            // 追加成string
            string.append((char) data);
        }
        return string.toString();
    }


    public AmountInfo reverseAmount(JSONObject limitAmount, String account) {
        AmountInfo amount = new AmountInfo();
        amount.setCurrency(limitAmount.get("currency").toString());
        amount.setValue(limitAmount.get("value").toString());
        amount.setIssuer(account);
        return amount;
    }

    public String txnType(Map tx, String account) {
        if ((tx.get("Account") != null && tx.get("Account").toString().equals(account)) || (tx.get("Target") != null && tx.get("Target").toString().equals(account)) ||
                (tx.get("Destination") != null && tx.get("Destination").toString().equals(account)) || (tx.get("LimitAmount") != null && ((Map) tx.get("LimitAmount")).get("issuer").toString().equals(account))) {
            switch (tx.get("TransactionType").toString()) {
                case "Payment":
                    return tx.get("Account") != null && tx.get("Account").toString().equals(account) ?
                            tx.get("Destination") != null && tx.get("Destination").toString().equals(account) ? "convert" : "sent" : "received";
                case "OfferCreate":
                    return "offernew";
                case "OfferCancel":
                    return "offercancel";
                case "TrustSet":
                    return tx.get("Account") != null && tx.get("Account").toString().equals(account) ? "trusting" : "trusted";
                case "RelationDel":
                case "AccountSet":
                case "SetRegularKey":
                case "RelationSet":
                case "SignSet":
                case "Operation":
                case "ConfigContract":
                    // TODO to sub-class tx type
                    return tx.get("TransactionType").toString().toLowerCase();
                default:
                    // TODO CHECK
                    return "unknown";
            }
        } else {
            return "offereffect";
        }
    }

    public AmountInfo parseAmount(Object tx) {
        if (tx != null) {
            AmountInfo amount = new AmountInfo();
            if (tx instanceof String) {
                amount.setCurrency(Config.CURRENCY);
                amount.setValue(Utils.amountFormatDivide(tx.toString()));
                amount.setIssuer("");
            } else if (tx instanceof AmountInfo) {
                return (AmountInfo) tx;
            } else if (tx instanceof Object) {
                JSONObject jsonObject = (JSONObject) tx;
                amount.setCurrency(jsonObject.get("currency").toString());
                amount.setValue(jsonObject.get("value").toString());
                amount.setIssuer(jsonObject.get("issuer").toString());
            }
            return amount;
        } else {
            return null;
        }
    }


    public boolean isNum(String amount) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        Matcher matcher = pattern.matcher(amount);
        return matcher.matches();
    }

    public boolean isValidAmount(Map amount) {
        if ((amount.get("value") != null && (amount.get("value").toString()).equals("0")) || !isNum(amount.get("value").toString())) {
            return false;
        }
        if (amount.get("currency") == null || !isValidCurrency(amount)) {
            return false;
        }
        if (amount.get("currency").toString().equals("SWT") && amount.get("issuer") != null) {
            return false;
        }
        if (amount.get("currency").toString().equals("SWT")) {
            //todo !baselib.isValidAddress(amount.issuer)
            return false;
        }
        return true;
    }

    public boolean isValidCurrency(Map amount) {
        if (amount.get("currency") == null || !(amount.get("currency") instanceof String)
                || amount.get("currency").toString().equals("")) {
            return false;
        }
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9]{3,6}|[A-F0-9]{40})$");
        Matcher matcher = pattern.matcher(amount.get("currency").toString());
        return matcher.matches();
    }

    /**
     * 根据金额对象内容返回信息
     * 货币单位SWT转基本单位
     *
     * @param amount 金额对象
     * @return
     */
    private Object toAmount(AmountInfo amount) {
        String value = amount.getValue();
        BigDecimal temp = new BigDecimal(value);
        BigDecimal max_value = new BigDecimal("100000000000");
        String currency = amount.getCurrency();
        if (StringUtils.isNotEmpty(value) && temp.compareTo(max_value) > 0) {
            throw new RemoteException("invalid amount: amount's maximum value is 100000000000");
        }
        if (currency.equals("SWT")) {
            BigDecimal exchange_rate = new BigDecimal("1000000.00");
            BigDecimal rs = temp.multiply(exchange_rate);
            return String.valueOf(rs.longValue());
        }
        return amount;
    }


    /**
     * 提交信息
     *
     * @return
     */
    public String submit(Map params) {
        params.remove("message");
        String msg = this.conn.submit(params);
        return msg;
    }

    /**
     * 监听信息transactions
     *
     * @return
     */

    public void transactions() {
        RemoteInter romteInter = new TransactionsImpl();
        Request request = new Request(this, "subscribe");
        romteInter.submit(request);
    }

    /**
     * 监听信息ledger
     *
     * @return
     */
    public void ledge() {
        RemoteInter romteInter = new LedgerCloseImpl();
        Request request = new Request(this, "subscribe");
        romteInter.submit(request);
    }


    public static void onLedgerClosed(String message) {
        //System.out.println("ledger:"+message);
    }

    public static void onTransaction(String message) {
        //System.out.println("tx:"+message);
    }

    /**
     * 监听
     *
     * @param type
     */
    public String on(String type) throws Exception {
        if (conn == null) {
            return "";
        }
        if (type.equals("removeListener")) {
            return "";
        }
        if (type.equals("transactions")) {
            this.transactions();
        } else if (type.equals("ledger_closed")) {
            this.ledge();
        }
        return "";
    }


    /************ setter and getter ************/
    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Boolean getLocal_sign() {
        return localSign;
    }

    public void setLocal_sign(Boolean local_sign) {
        this.localSign = local_sign;
    }


    /**********************返回transaction对象方法*************************/

    /**
     * 获取交易对象
     *
     * @param account 发起账号
     * @param to      目标账号
     * @param amount  支付金额对象Amount
     * @return Transaction
     */
    public Transaction buildPaymentTx(String account, String to, AmountInfo amount) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setTo(to);
        // 校验,并将参数写入tx_json对象
        Map tx_json = new HashMap();
        if (!CheckUtils.isValidAddress(account)) {
            throw new RemoteException("invalid source address");
        }
        if (!CheckUtils.isValidAddress(to)) {
            throw new RemoteException("invalid destination address");
        }
        tx_json.put("TransactionType", "Payment");
        tx_json.put("Account", account);
        tx_json.put("Amount", toAmount(amount));
        tx_json.put("Destination", to);
        tx.setCommand("submit");
        tx.setTxJson(tx_json);
        tx.setConn(conn);
        tx.setRemote(this);
        tx.setLocalSign(localSign);
        return tx;
    }

    public Boolean getLocalSign() {
        return localSign;
    }

    public void setLocalSign(Boolean localSign) {
        this.localSign = localSign;
    }

    public String getStatus() {
        return conn.getState();
    }


}
