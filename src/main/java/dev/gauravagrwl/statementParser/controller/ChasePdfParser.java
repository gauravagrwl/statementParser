package dev.gauravagrwl.statementParser.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChasePdfParser {
    private static Logger logger = LoggerFactory.getLogger(ChasePdfParser.class);

    //TODO: 
    //1. Select Year - Done read from File name.
    //2. Remove Sepecial Characters from Number and then convert. - Done
    //3. Use heading to find out if it is deposit or withdrawal. Need to use Pattern Matching to identify the transactions type or constants.
    //4. Identify Accont Type: Checking or Savings.
    //5. Handle multiple line trans details.
    
    private static String DEBIT_TRANS[] = {"Card Purchase", "Transfer To", "Payment To"};
    private static String CREDIT_TRANS[] = {"Card Purchase", "Transfer To"};

    private static String YEAR_EXTENSION;
    private static DateTimeFormatter check = DateTimeFormatter.ofPattern("MM/dd/uuuu");
    private static List<String> exclusions = new ArrayList<>(Arrays.asList("Payment Thank You", "AUTOMATIC PAYMENT"));

    public static List<ChaseRecord> parse(String data, String year) {
    	YEAR_EXTENSION = "/" + year;
        List<ChaseRecord> l = new ArrayList<>();
        
        List<ChaseTrans> chaseTrans = new ArrayList<>();
        
		for (String line : data.split("\n")) {
			System.out.println("Processing Line: " + line);
			String[] split = line.split("\\s");
			if (split.length > 0) {
				String test = split[0];
				if (isMMDD(test)) {
					ChaseRecord cr = new ChaseRecord();
					cr.date = extractDate(test);
					try {
						System.out.println("Processing Line: " + line);
						System.out.println("Processing split: " + split);
						String last = split[split.length - 1];
						last = last.replaceAll(",", "");
						last = last.replaceAll("[^0-9.]", " ");
						cr.amt = Double.parseDouble(last);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					cr.desc = String.join(" ", Arrays.copyOfRange(split, 1, split.length - 1));
					cr.desc = cr.desc.replaceAll("\\s\\s+", " ");
					cr.transactionType = determineTransactionType(cr.desc);
					l.add(cr);
				}
			}
		}
        /*
        for (String line : data.split("\n")) {
            
        	System.out.println(">>>>>>: " + line);
            
            if (line.isEmpty()) continue;
            
            String[] split = line.split("\\s");
            
            if (split == null || split.length == 0) continue;
            
            String test = split[0];
            
            if (!isMMDD(test)) continue;
            
            if (skip(line)) continue;
            
            if (split.length < 4) continue;
            
            ChaseRecord cr = new ChaseRecord();
            cr.date = extractDate(test);
            try {
                String last = split[split.length - 1];
                last = last.replaceAll(",", "");
                last = last.replaceAll("[^0-9.]", " ");
                cr.amt = Double.parseDouble(last);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            cr.desc = String.join(" ", Arrays.copyOfRange(split, 1, split.length - 1));
            cr.desc = cr.desc.replaceAll("\\s\\s+", " ");
            l.add(cr);
        }
        */
        return l;
    }

    private static String determineTransactionType(String desc) {
		if(StringUtils.containsAny(desc, DEBIT_TRANS))
			return "Debit";
		return "";
	}

	private static boolean skip(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }
        for (String e : exclusions) {
            if (s.contains(e)) {
                return true;
            }
        }
        return false;
    }

    protected static LocalDate extractDate(String s) {
        if (!isMMDD(s)) {
            return null;
        }
        LocalDate localDate = LocalDate.parse(s + YEAR_EXTENSION, check);
        return localDate;
    }

    public static boolean isMMDD(String s) {
    	if (s == null || s.isEmpty() || s.length() != 5) {
            return false;
        }
        try {
            s += YEAR_EXTENSION;
            LocalDate.parse(s, check);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class ChaseRecord {
    	public String accountType;
        public String transactionType;
        public LocalDate date;
        public String desc;
        public Double amt;

        @Override
        public String toString() {
            return "ChaseRecord{" + "date=" + date + ", desc='" + desc + '\'' + ", amt=" + amt + '}';
        }
    }
    
	public static class ChaseTrans {
		public String transactionType;
		public List<Transaction> transaction;
	}

	public class Transaction {
		public LocalDate date;
		public String desc;
		public Double amt;
	}
}
