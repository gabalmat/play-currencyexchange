package models;

import java.util.*;
import javax.persistence.*;

import io.ebean.*;

@Entity
public class Log extends Model {
	
	@Id
	public Long id;
	public String transactionId;
	public Date timestamp;
	public Integer amountBtc;
	
	public static final Finder<Long, Log> find = new Finder<>(Log.class);
}
