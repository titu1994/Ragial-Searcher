package com.ragialquery.ui;

import com.ragialquery.data.RagialData;
import com.ragialquery.data.RagialQueryMatcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

/**
 * A GUI for displaying the query results.
 * @author Somshubra Majumdar
 * @version 2.0
 * @since 2.0
 */
public class UIHandler extends JFrame {
	private RagialQueryMatcher matcher;
	private static UIHandler handler;
	
	
	public UIHandler() {
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		setExtendedState(Frame.MAXIMIZED_BOTH);
		matcher = RagialQueryMatcher.getMatcher();
		
		getContentPane().setBackground(Color.WHITE);
		getContentPane().setLayout(null);
		
		JLabel lblSearchRagial = new JLabel("Search Ragial");
		lblSearchRagial.setBounds(37, 23, 199, 14);
		getContentPane().add(lblSearchRagial);
		
		textSearchVal = new JTextField();
		textSearchVal.setBounds(152, 20, 265, 20);
		getContentPane().add(textSearchVal);
		textSearchVal.setColumns(10);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isSpecific()) {
					RagialData data = matcher.searchRagialSpecificly(textSearchVal.getText());
					setupData(data);
				}
				else {
					RagialData datas[] = null;
					try {
						datas = matcher.searchRagial(textSearchVal.getText(), isSpecific()).get();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} catch (ExecutionException e1) {
						e1.printStackTrace();
					}
					
					//RagialData data = RagialQueryMatcher.searchRagialSpecificly(textSearchVal.getText(), datas);
					setupData(datas[0]);
				}
				
				textSearchVal.setText("");
			}
		});
		btnSearch.setBounds(479, 19, 89, 23);
		getContentPane().add(btnSearch);
		
		JLabel lblName = new JLabel("Name");
		lblName.setBounds(37, 71, 46, 14);
		getContentPane().add(lblName);
		
		lblNameVal = new JLabel("");
		lblNameVal.setBounds(118, 71, 265, 14);
		getContentPane().add(lblNameVal);
		
		JLabel lblShortCost = new JLabel("Short Count");
		lblShortCost.setBounds(37, 101, 105, 14);
		getContentPane().add(lblShortCost);
		
		JLabel lblShortMin = new JLabel("Short Min");
		lblShortMin.setBounds(152, 101, 84, 14);
		getContentPane().add(lblShortMin);
		
		JLabel lblShortMax = new JLabel("Short Max");
		lblShortMax.setBounds(254, 101, 100, 14);
		getContentPane().add(lblShortMax);
		
		JLabel lblShortAvg = new JLabel("Short Avg");
		lblShortAvg.setBounds(364, 101, 108, 14);
		getContentPane().add(lblShortAvg);
		
		JLabel lblShortConfidence = new JLabel("Short Confidence");
		lblShortConfidence.setBounds(482, 101, 86, 14);
		getContentPane().add(lblShortConfidence);
		
		lblShortCostVal = new JLabel("");
		lblShortCostVal.setBounds(37, 126, 105, 14);
		getContentPane().add(lblShortCostVal);
		
		lblShortMinVal = new JLabel("");
		lblShortMinVal.setBounds(152, 126, 92, 14);
		getContentPane().add(lblShortMinVal);
		
		lblShortMaxVal = new JLabel("");
		lblShortMaxVal.setBounds(254, 126, 100, 14);
		getContentPane().add(lblShortMaxVal);
		
		lblShortAvgVal = new JLabel("");
		lblShortAvgVal.setBounds(364, 126, 108, 14);
		getContentPane().add(lblShortAvgVal);
		
		lblShortConfidenceVal = new JLabel("");
		lblShortConfidenceVal.setBounds(479, 126, 86, 14);
		getContentPane().add(lblShortConfidenceVal);
		
		JLabel lblLongCost = new JLabel("Long Count");
		lblLongCost.setBounds(37, 178, 105, 14);
		getContentPane().add(lblLongCost);
		
		JLabel lblLongMin = new JLabel("Long Min");
		lblLongMin.setBounds(152, 178, 84, 14);
		getContentPane().add(lblLongMin);
		
		JLabel lblLongMax = new JLabel("Long Max");
		lblLongMax.setBounds(254, 178, 100, 14);
		getContentPane().add(lblLongMax);
		
		JLabel lblLongAvg = new JLabel("Long Avg");
		lblLongAvg.setBounds(364, 178, 108, 14);
		getContentPane().add(lblLongAvg);
		
		JLabel lblLongConfidence = new JLabel("Long Confidence");
		lblLongConfidence.setBounds(482, 178, 86, 14);
		getContentPane().add(lblLongConfidence);
		
		lblLongCostVal = new JLabel("");
		lblLongCostVal.setBounds(37, 203, 105, 14);
		getContentPane().add(lblLongCostVal);
		
		lblLongMinVal = new JLabel("");
		lblLongMinVal.setBounds(152, 203, 92, 14);
		getContentPane().add(lblLongMinVal);
		
		lblLongMaxVal = new JLabel("");
		lblLongMaxVal.setBounds(254, 203, 100, 14);
		getContentPane().add(lblLongMaxVal);
		
		lblLongAvgVal = new JLabel("");
		lblLongAvgVal.setBounds(364, 203, 108, 14);
		getContentPane().add(lblLongAvgVal);
		
		lblLongConfidenceVal = new JLabel("");
		lblLongConfidenceVal.setBounds(479, 203, 86, 14);
		getContentPane().add(lblLongConfidenceVal);
		
		chckbxSpecificSearch = new JCheckBox("Specific Search");
		chckbxSpecificSearch.setBounds(37, 41, 199, 23);
		getContentPane().add(chckbxSpecificSearch);
	}

	protected void setupData(RagialData data) {
		lblNameVal.setText(data.name);
		lblShortCostVal.setText(data.shortNumber + "");
		lblShortMinVal.setText(data.shortMin + "");
		lblShortMaxVal.setText(data.shortMax + "");
		lblShortAvgVal.setText(data.shortAverage + "");
		lblShortConfidenceVal.setText(data.shortConfidence + "");
		lblLongCostVal.setText(data.longNumber + "");
		lblLongMinVal.setText(data.longMin + "");
		lblLongMaxVal.setText(data.longMax + "");
		lblLongAvgVal.setText(data.longAverage + "");
		lblLongConfidenceVal.setText(data.longConfidence + "");
	}

	private static final long serialVersionUID = 6147759135067713202L;
	private JTextField textSearchVal;
	private JLabel lblNameVal;
	private JLabel lblShortCostVal;
	private JLabel lblShortMinVal;
	private JLabel lblShortMaxVal;
	private JLabel lblShortAvgVal;
	private JLabel lblShortConfidenceVal;
	private JLabel lblLongCostVal;
	private JLabel lblLongMinVal;
	private JLabel lblLongMaxVal;
	private JLabel lblLongAvgVal;
	private JLabel lblLongConfidenceVal;
	private JCheckBox chckbxSpecificSearch;

	public static void main(String[] args) {
		handler = new UIHandler();
		handler.setVisible(true);
	}
	
	public JLabel getLblNameVal() {
		return lblNameVal;
	}
	public JLabel getLblShortCostVal() {
		return lblShortCostVal;
	}
	public JLabel getLblShortMinVal() {
		return lblShortMinVal;
	}
	public JLabel getLblShortMaxVal() {
		return lblShortMaxVal;
	}
	public JLabel getLblNShortAvgVal() {
		return lblShortAvgVal;
	}
	public JLabel getLblShortConfidenceVal() {
		return lblShortConfidenceVal;
	}
	public JLabel getLblLongCostVal() {
		return lblLongCostVal;
	}
	public JLabel getLblLongMinVal() {
		return lblLongMinVal;
	}
	public JLabel getLblLongMaxVal() {
		return lblLongMaxVal;
	}
	public JLabel getLblLongAvgVal() {
		return lblLongAvgVal;
	}
	public JLabel getLblLongConfidenceVal() {
		return lblLongConfidenceVal;
	}
	public JTextField getTextSearchVal() {
		return textSearchVal;
	}
	public boolean isSpecific() {
		return chckbxSpecificSearch.isSelected();
	}
}
