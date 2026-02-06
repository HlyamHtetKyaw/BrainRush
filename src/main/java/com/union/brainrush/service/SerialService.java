//package com.union.brainrush.service;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//
//import com.union.brainrush.ui.Question;
//import com.union.brainrush.ui.Setting;
//import com.union.brainrush.ui.UiConstant;
//
//import javafx.application.Platform;
//import javafx.concurrent.Service;
//import javafx.concurrent.Task;
//import purejavacomm.CommPortIdentifier;
//import purejavacomm.SerialPort;
//
//public class SerialService extends Service<Void> {
//	private SerialPort serialPort;
//	private BufferedReader reader;
//	private OutputStream outputStream;
//	public static boolean running = true;
//	String lastLine = "";
//	@Override
//	protected Task<Void> createTask() {
//		return new Task<>() {
//			@Override
//			protected Void call() {
//				try {
//					// Replace with your actual port name
//					String portName = null;
//					try {
//						portName = Setting.comboBox.getValue();
//					}catch(NullPointerException e) {
//						System.out.println("Set COM value in Setting");
//					}
//					CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
//
//					// Open the port
//					serialPort = (SerialPort) portId.open("SerialReader", 2000);
//					serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
//							SerialPort.PARITY_NONE);
//
//					outputStream = serialPort.getOutputStream();
//					reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
//					sendMessage(Player.sentMessage);
//
//					System.out.println("Listening for messages...");
//					String line;
//
//					while (running && (line = reader.readLine()) != null) {
////                    	sendMessage(Player.sentMessage);
//
//
//						// Step 2: Process messages from Arduino
//						if (line.startsWith("AR:")) {
//							String fValue = extractValue(line, "f");
//							String sValue = extractValue(line, "s");
//							String tValue = extractValue(line, "t");
//							String counterValue = extractValue(line, "c");
//							String playerQuantity = String.valueOf(Player.playerQuantity);
//							printOutPut(line, lastLine);
//							if (playerQuantity.equals("1")) {
//								if (fValue.length()!=0) {
//
//									String fAnswer = fValue;
//									Player.fPlayerAns = fAnswer;
//									Question.playerSlot[0].setImage(UiConstant.firstPlayerConfirm);
//									sendMessage("PC:{choice:False}");
//									running = false;
//									Platform.runLater(() -> {
//										Question.actionButton.fire();
//									});
//								}
//							} else if (playerQuantity.equals("2")) {
//								if (fValue.length()!=0) {
//
//									String fAnswer = fValue;
//									Player.fPlayerAns = fAnswer;
//									Question.playerSlot[0].setImage(UiConstant.firstPlayerConfirm);
//								}
//								if (sValue.length()!=0) {
//									String sAnswer = sValue;
//									Player.sPlayerAns = sAnswer;
//									Question.playerSlot[1].setImage(UiConstant.secondPlayerConfirm);
//								}
//								if (counterValue.equals("2")) {
//									sendMessage("PC:{choice:False}");
//									running = false;
//									Platform.runLater(() -> {
//										Question.actionButton.fire();
//									});
//								}
//							} else if (playerQuantity.equals("3")) {
//								if (fValue.length()!=0) {
//									String fAnswer = fValue;
//									Player.fPlayerAns = fAnswer;
//									Question.playerSlot[0].setImage(UiConstant.firstPlayerConfirm);
//								}
//								if (sValue.length()!=0) {
//									String sAnswer = sValue;
//									Player.sPlayerAns = sAnswer;
//									Question.playerSlot[1].setImage(UiConstant.secondPlayerConfirm);
//								}
//								if (tValue.length()!=0) {
//									String tAnswer = tValue;
//									Question.playerSlot[2].setImage(UiConstant.thirdPlayerConfirm);
//									Player.tPlayerAns = tAnswer;
//								}
//								if (counterValue.equals("3")) {
//									sendMessage("PC:{choice:False}");
//									running = false;
//									Platform.runLater(() -> {
//										Question.actionButton.fire();
//									});
//								}
//							}
//
//						} else {
////							System.out.println("Ignoring non-AR message.");
//						}
//					}
//				} catch (Exception e) {
//					System.err.println("Error: " + e.getMessage());
//				}
//				return null;
//			}
//		};
//	}
//	private void printOutPut(String line,String lastLine) {
//		if (!line.equals(lastLine)) { // Print only if different from the last line
//	        System.out.println("Received: " + line);
//	        lastLine = line; // Update last received line
//	    }
//	}
//	public void sendMessage(String message) {
//		try {
//			if (outputStream != null) {
//				outputStream.write((message + "\n").getBytes()); // Send data to Arduino
//				outputStream.flush();
//				System.out.println("Sent: " + message);
//			}
//		} catch (IOException e) {
//			System.err.println("Error sending message: " + e.getMessage());
//		}
//	}
//
//	public void stopService() {
//		running = false;
//		sendMessage("PC:{choice:False}"); // Tell Arduino to stop processing input
//		cancel();
//		try {
//			if (reader != null)
//				reader.close();
//			if (outputStream != null)
//				outputStream.close();
//			if (serialPort != null)
//				serialPort.close();
//		} catch (IOException e) {
//			System.err.println("Error closing serial port: " + e.getMessage());
//		}
//	}
//
//	private String extractValue(String line, String key) {
//		String pattern = key + ":\\[(.*?)\\]";
//		java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(line);
//		return matcher.find() ? matcher.group(1) : "";
//	}
//}
