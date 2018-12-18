package services;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.ebean.Expr;
import models.Log;
import play.db.ebean.Transactional;

import java.util.Date;
import java.util.List;

public class LogService {

	@Transactional
	public Long createLog(ObjectNode object) {
		Log log = new Log();
		
		log.transactionId = object.get("transactionId").asText();
		log.timestamp = new Date();
		
		if (object.has("amountBtc")) {
			log.amountBtc = object.get("amountBtc").asInt();
		}
		
		log.save();
		
		return log.id;
	}
	
	@Transactional
	public static List<String> getTransactionIds() {
		List<String> list = Log.find.query().select("transactionId").where(Expr.isNotNull("amountBtc")).findSingleAttributeList();
		return list;
	}
	
	@Transactional
	public static Log getTransaction(String transactionId) {
		List<Log> entries = Log.find.query().select("transactionId").where(Expr.and(Expr.isNotNull("amountBtc"), Expr.eq("transactionId", transactionId))).findList();
		if (!entries.isEmpty()) {
			Log entry = entries.get(0);
			return entry;
		}
		return null;
	}
}
