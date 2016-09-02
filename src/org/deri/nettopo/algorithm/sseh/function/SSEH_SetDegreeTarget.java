package org.deri.nettopo.algorithm.sseh.function;


import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.NetTopoApp;

/**Set some values while run sseh algorithm
 * @author panhao
 *
 */
public class SSEH_SetDegreeTarget implements AlgorFunc {
	private Algorithm algorithm;
	private SSEH_MAIN sseh;
	private NetTopoApp app;
	/***********Windows Resource**********/
	private JFrame frame;
	private Image titleImg;
	private JLabel lbInto;
	private JLabel lbEsValue;
	private JTextField DegreeTarget;
	private JButton btnSub;
	
	public SSEH_SetDegreeTarget(Algorithm algorithm) {
		this.algorithm = algorithm;
		sseh=new SSEH_MAIN();
	}
	@Override
	public void run() {
		//Before we set values, we should stop this algorithm
		app=NetTopoApp.getApp();
		app.cmd_stopAlgorithm();
		System.out.println("Run set DegreeTarget function");
		frame=new JFrame("Set DegreeTarget");
		frame.setLayout(null);
		frame.setBounds(400, 400, 400, 300);
		lbInto = new JLabel("Set the DegreeTarget");
		lbInto.setBounds(20, 10,300,20);
		frame.add(lbInto);
		lbEsValue=new JLabel("DegreeTarget:");
		lbEsValue.setBounds(20, 40, 150, 20);
		frame.add(lbEsValue);
		DegreeTarget=new JTextField(10);
		DegreeTarget.setBounds(200, 40, 50, 20);
		frame.add(DegreeTarget);
		
		try {
			titleImg=ImageIO.read(this.getClass().getResource("/org/deri/nettopo/image/logo.jpg"));
			frame.setIconImage(titleImg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		btnSub=new JButton("Finish");
		btnSub.setBounds(150, 150, 70, 20);
		btnSub.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				System.out.println("Finish set values");
				if(DegreeTarget.getText().equals("")){
					JOptionPane.showMessageDialog(frame.getComponent(0),"Please enter the values!", "error", JOptionPane.ERROR_MESSAGE);
				}
				else{
					int Degree =Integer.parseInt(DegreeTarget.getText());
					sseh.setDegreeTarget(Degree);
					System.out.println("set DegreeTarget:"+ Degree);
					frame.dispose();
				}
				
			}
		});
		frame.add(btnSub);
		frame.setVisible(true);

	}
	/**************GETTER AND SETTER**************/
	public Algorithm getAlgorithm() {
		return this.algorithm;
	}

}
