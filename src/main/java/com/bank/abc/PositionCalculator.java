package com.bank.abc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class PositionCalculator {
	/**
	 * This method reads position and transaction file location as input arguments.
	 */
	public static void main(String[] args) {
		try {
			/* Validation to check total input parameters. 
			 */
			if(args.length<2) {
				System.out.println("position and transaction file paths are not given!!");
				System.out.println("Syntax: java -cp <jarfilepath> com.bank.abc.PositionCalculator <positionfilepath> <transactionfilepath>");
				System.exit(1);
			}else {
				//Reads position file location from the first argument.
				String positionFile=args[0];
				//Reads transaction file location from the second argument.
				String transactionFile=args[1];
				//If position file doesn't exist then program shall terminate.
				if (Files.notExists(Paths.get(positionFile))) {
					System.out.println("Position file does not exist:"+positionFile);
					System.exit(1);
				}
				//If transaction file doesn't exist then program shall terminate.
				if (Files.notExists(Paths.get(transactionFile))) {
					System.out.println("Transaction file does not exist:"+transactionFile);
					System.exit(1);
				}
				PositionCalculator positionCalculator =new PositionCalculator();
				Map<String,List<Position>> openPositions=positionCalculator.loadPositions(positionFile);
				Map<String,List<Position>> closePositions=positionCalculator.loadPositions(positionFile);
				//positionCalculator.displayPositions(openPositions);
				List<Transaction> transactions=positionCalculator.loadTransactions(transactionFile);
				closePositions=positionCalculator.calculateClosePositions(closePositions, transactions);
				//positionCalculator.displayPositions(closePositions);
				List<EODPosition> eodPositions=positionCalculator.calculateDelta(openPositions, closePositions);
				//positionCalculator.display2(eodPositions);
				String outputFilePath=Paths.get(positionFile).getParent()+File.separator+"Expected_EndOfDay_Positions.txt";
				System.out.println("EndOfDay_Positions file location:"+outputFilePath);
				positionCalculator.saveEODPositionsToFile(outputFilePath,eodPositions);
				List<EODPosition> largestAndLowestVolume=positionCalculator.findLargestAndLowestVolumeInstrument(eodPositions);
				System.out.println("Instrument with largest traded volume:"+largestAndLowestVolume.get(0).getInstrument());
				System.out.println("Instrument with lowest traded volume:"+largestAndLowestVolume.get(largestAndLowestVolume.size()-1).getInstrument());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public Map<String,List<Position>> loadPositions(String positionFile) {
		Map<String,List<Position>> mapPositions=new HashMap<String,List<Position>> ();
        String line = "";
        String csvDelimiter = ",";
        try (BufferedReader br = new BufferedReader(new FileReader(positionFile))) {
        	boolean headerRow=true;
            while ((line = br.readLine()) != null) {
            	if(headerRow) {
            		headerRow=false;
            		continue;
            	}
            	try {
	                String[] columns = line.split(csvDelimiter);
	                if(columns!=null && columns.length==4) {
	                	if (columns[0]!=null && columns[1]!=null && columns[2]!=null && columns[3]!=null) {
	                		if (!columns[0].trim().isEmpty() && !columns[2].trim().isEmpty()) {
	                			int account=Integer.parseInt(columns[1]);
	                			long quantity=Long.parseLong(columns[3]);
	                			Position positions=new Position(columns[0], account, columns[2], quantity);
	                			List<Position> listPositions=new ArrayList<Position>();
	                			if(mapPositions.containsKey(columns[0])) {
	                				listPositions=mapPositions.get(columns[0]);
	                				listPositions.add(positions);
	                			}else {
	                				listPositions.add(positions);
	                			}
	                			mapPositions.put(columns[0], listPositions);
	                    	}
	                	}
	                }
            	}catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapPositions;
    }

	public List<Transaction>  loadTransactions(String transactionFile) {
        List<Transaction> listTransactions=new ArrayList<Transaction>();
        try (FileReader reader = new FileReader(transactionFile))
        {
        	Iterator<JsonElement> temp=((JsonArray) new JsonParser().parse(reader)).iterator();
            while(temp.hasNext()) {
            	JsonElement jsonElement=temp.next();
            	Gson gson = new Gson();
            	Transaction trans=gson.fromJson(jsonElement, Transaction.class);
            	listTransactions.add(trans);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listTransactions;
    }

	public Map<String,List<Position>> calculateClosePositions(Map<String,List<Position>> closePositions, List<Transaction> transactions) {
		for(Transaction trans:transactions) {
			if(closePositions.containsKey(trans.getInstrument())) {
				List<Position> listPositions=closePositions.get(trans.getInstrument());
				for(int index=0;index<listPositions.size(); index++) {
					Position position=listPositions.get(index);
					long quantity=0;
					if("B".equalsIgnoreCase(trans.getTransactionType())){
						if("E".equals(position.getAccountType())) {
							quantity=position.getQuantity() + trans.getTransactionQuantity();
						}
						else if("I".equals(position.getAccountType())) {
							quantity=position.getQuantity() - trans.getTransactionQuantity();
						}
					}
					else if("S".equalsIgnoreCase(trans.getTransactionType())){
						if("E".equals(position.getAccountType())) {
							quantity=position.getQuantity() - trans.getTransactionQuantity();
						}
						else if("I".equals(position.getAccountType())) {
							quantity=position.getQuantity() + trans.getTransactionQuantity();
						}
					}
					position.setQuantity(quantity);
					listPositions.set(index, position);
				}
				closePositions.put(trans.getInstrument(), listPositions);
			}
		}
		return closePositions;
	}

	public List<EODPosition> calculateDelta(Map<String,List<Position>> openPositionMap, Map<String,List<Position>> closePositionMap) {
		Set<String> instruments=openPositionMap.keySet();
		List<EODPosition> eodPositions=new ArrayList<EODPosition>();
		for(String instrument: instruments) {
			List<Position> openPositionList=openPositionMap.get(instrument);
			List<Position> closePositionList=closePositionMap.get(instrument);
			for(Position openPostion:openPositionList ) {
				for(Position closePostion:closePositionList ) {
					if(openPostion.getAccountType().equals(closePostion.getAccountType())) {
						long delta=closePostion.getQuantity()-openPostion.getQuantity();
						EODPosition eodPosition=new EODPosition(instrument,closePostion.getAccount(),closePostion.getAccountType(),closePostion.getQuantity(),delta);
						eodPositions.add(eodPosition);
					}
				}
			}
		}
		return eodPositions;
	}

	public void saveEODPositionsToFile(String outputFilePath,List<EODPosition> eodPositions) {
		try{
			List<String> fileContents=new ArrayList<String>();
			fileContents.add("Instrument,Account,AccountType,Quantity,Delta");
			for(EODPosition position:eodPositions) {
				String str=position.getInstrument()+","+position.getAccount()+","+position.getAccountType()+","+position.getQuantity()+","+position.getDelta();
				fileContents.add(str);
			}
			Path path = Paths.get(outputFilePath);
			if(Files.notExists(path)) {
				Files.write(path, "".getBytes(), StandardOpenOption.CREATE);
			}
			Files.write(path, fileContents, StandardOpenOption.TRUNCATE_EXISTING);
		}catch(Exception ex){
			System.out.println("Failed to update stock status to file " + outputFilePath);
			ex.printStackTrace();
		}
	}
	
	public List<EODPosition> findLargestAndLowestVolumeInstrument(List<EODPosition> eodPositions) {
		List<EODPosition> internalAccounts=new ArrayList<EODPosition>();
		for(EODPosition temp: eodPositions) {
			if("I".equals(temp.getAccountType())) {
				internalAccounts.add(temp);
			}
		}
		Collections.sort(internalAccounts, new Comparator<EODPosition>() {
			public int compare(EODPosition first, EODPosition second) {
				if (first.delta > second.delta)
					return -1;
				else if (first.delta < second.delta)
					return 1;
				else {
					if (first.delta > second.delta)
						return -1;
					else
						return 1;
				}
			}
		});
		return internalAccounts;
	}

	public void displayPositions(Map<String,List<Position>> mapPositions) {
		try{
			Set<String> instruments=mapPositions.keySet();
			List<Position> positions=new ArrayList<Position>();
			for(String instrument: instruments) {
				positions=mapPositions.get(instrument);
				for(Position position : positions) {
					System.out.println(position.getInstrument()+","+position.getAccount()+","+position.getAccountType()+","+position.getQuantity());
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void display2(List<EODPosition> eodPosition) {
		try{
			for(EODPosition position : eodPosition) {
				System.out.println(position.getInstrument()+","+position.getAccount()+","+position.getAccountType()+","+position.getQuantity()+","+position.getDelta());
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
