// Copyright (C) 2015 Yuya Chiba. All Rights Reserved.

//<applet code = "Control.class" width = "542" height = "568"></applet>
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.JOptionPane;

public class Control extends Applet{
	static final int MESH = 10;
	static final int CANVASW = 541;
	static final int CANVASH = 541;
	static final int LSPACE = 40;
	static Random r = new Random();
	static int map[][][] = new int[10][10][3];	
	static final double A_control = 0.417;
	static double B_control = 1.0;			
	static int countA = 0, countB = 0, countDRAW = 0;
	static int winA = 0, winB = 0;
	
	public void init() {
		DrawCanvas dc = new DrawCanvas(CANVASW, CANVASH, this);
		InfoPanel ip = new InfoPanel(this);
		ControlPanel cp = new ControlPanel(this, dc, ip);
		
		setLayout(new BorderLayout());
		
		add(dc, "Center");
		add(cp, "North");
		add(ip, "South");
	}
	
	public void datacreation(double B_seed) {
	        int Seles_Amount = 35000;
		int x = 0, y = 0;
		double max = 0.0;
		
		B_control = B_seed;
		countA = countB = countDRAW = 0;
		
		/* 初期設定 */
		for(int i = 0; i < MESH; i++)  {
			for(int j = 0; j < MESH; j++)  {
				map[i][j][2] = GetRandom(1000, 2000);         //その地域の人口
				map[i][j][0] = GetRandom(0, map[i][j][2]/4);  //その地域のA社の販売数
				map[i][j][1] = GetRandom(0, map[i][j][2]/4);  //その地域のB社の販売数
			}
		}

		/* A社の市場占拠率が40%に達する地区を1つ確保 */
	
		/* A社の市場占拠率が最大の地域を検索 */
		for(int i = 0; i < MESH; i++)  {
			for(int j = 0; j < MESH; j++)  {
				if(((double)map[i][j][0] / (double)map[i][j][2]) > max)  {
					max = (double)map[i][j][0] / (double)map[i][j][2];
					y = i;  x = j;
			    }
			}
	        }
		
		/* 上記で定めた地域の市場占拠率が41.7%になるまで販売 */
			while((double)map[y][x][0] / (double)map[y][x][2] < A_control)  {
				map[y][x][0]++;
				map[GetRandom(0,9)][GetRandom(0,9)][1]++;
	        }
		
		/* 他の地区を戦略 */
		for(int i = 0; i < Seles_Amount; i++)  {
			while(true)  {
				y = GetRandom(0,9); x = GetRandom(0,9);
				if((double)map[y][x][1] / (double)map[y][x][2] < A_control && (map[y][x][0] + map[y][x][1]) < map[y][x][2])
					break;
			}
			
			map[y][x][0]++;

			while(true)  {
				y = GetRandom(0,9); x = GetRandom(0,9);
				if((double)map[y][x][0] / (double)map[y][x][2] < B_control && (map[y][x][0] + map[y][x][1]) < map[y][x][2])
					break;
			}
			map[y][x][1]++;
		}
		
		/* 市場占拠率が高い地域の数をカウント */
		for(int i = 0; i < MESH; i++)  {
			for(int j = 0; j < MESH; j++)  {
				if(map[i][j][0] > map[i][j][1])  countA++;
				if(map[i][j][0] < map[i][j][1])  countB++;
				if(map[i][j][0] == map[i][j][1]) countDRAW++;
	 		}
		}
		
		/* A社,B社の勝ち負け */
		if(countA > countB)  winA++;
		if(countB > countA)  winB++;
	}
			
	public int GetRandom(int min, int max) {
		return r.nextInt((max - min) + 1) + min;
	}
}
 
class DrawCanvas extends Canvas {
	private int w, h;
	private Control p;
	
	public DrawCanvas(int wd, int ht, Control rw) {
		this.w = wd;
		this.h = ht;
		this.p = rw;
		
		setBackground(Color.gray);
	}
	
	public void paint(Graphics g) {
		int y_draw = 100;
		int x_draw = 475;
		
		for(int y1 = y_draw, y2 = 0; y1 < p.MESH * p.LSPACE + y_draw; y1 += p.LSPACE, y2++)  {
			for(int x1 = x_draw, x2 = 0; x1 < p.MESH * p.LSPACE + x_draw; x1 += p.LSPACE, x2++)   {
				
				g.setColor(Color.black);
				g.drawRect(x1, y1, p.LSPACE, p.LSPACE);
				
				if(p.map[y2][x2][0] > p.map[y2][x2][1])  {
					g.setColor(Color.red);
					g.fillRect(x1 + 1, y1 + 1, p.LSPACE - 2, p.LSPACE- 2);
				}
				if(p.map[y2][x2][0] < p.map[y2][x2][1])  {
					g.setColor(Color.blue);
					g.fillRect(x1 + 1, y1 + 1, p.LSPACE - 2, p.LSPACE - 2);
				}
				if(p.map[y2][x2][0] == p.map[y2][x2][1])  {
					g.setColor(Color.yellow);
					g.fillRect(x1 + 1, y1 + 1, p.LSPACE - 2, p.LSPACE - 2);
				}
			}
		}
	}
}

class ControlPanel extends Panel implements ActionListener, Runnable  {
	private Control p;
	private DrawCanvas d;
	private InfoPanel i;
	private Button b_paint, b_reset, b_auto;
	private TextField t_seed, t_autocount;
	public Thread thread;
	
	public ControlPanel(Control ca, DrawCanvas dc, InfoPanel ip) {
		this.p = ca;
		this.d = dc;
		this.i = ip;
		
		setGUI();
	}
	
	public void setGUI() {
		b_paint = new Button("Paint");
		add(b_paint);
		b_paint.addActionListener(this);
		
		add(new Label("B社:"));
		
		t_seed = new TextField(3);
		add(t_seed);
		
		add(new Label("%"));
		
		b_auto = new Button("Auto");
		add(b_auto);
		b_auto.addActionListener(this);
		
		t_autocount = new TextField(3);
		add(t_autocount);
		
		b_reset = new Button("Reset");
		add(b_reset);
		b_reset.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		double seed, autocount;
		String s_seed, s_autocount;
		
		if(e.getSource() == b_paint) {
			b_paint.setLabel("Renew");
			
		    s_seed = t_seed.getText();
		    if(s_seed.length() == 0) seed = 1.0;
		    else  seed = Double.parseDouble(s_seed) / 100.0;
		    
		    if(0.3 <= seed && seed <= 1.0))  {
		    	p.datacreation(seed);
		    	d.repaint();
		    	i.setString();
		    }else JOptionPane.showMessageDialog(null, "B社の値は30以上100以下にして下さい");
		}
		
		if(e.getSource() == b_reset) {
			
			p.countA = p.countB = p.countDRAW = 0;
			p.winA = p.winB = 0;
			
			for(int y = 0; y < p.MESH; y++)  
				for(int x = 0; x < p.MESH; x++)  
					p.map[y][x][0] = p.map[y][x][1] = 0;
			
			b_paint.setLabel("Paint");
			
			d.repaint();
			i.setString();
		}
		
		if(e.getSource() == b_auto) {
			s_seed = t_seed.getText();
			s_autocount = t_autocount.getText();
			
			if(s_seed.length() == 0) seed = 1.0;
			  else seed = Double.parseDouble(s_seed) / 100.0;
			
			if(s_autocount.length() == 0) autocount = 0;
			else autocount = Integer.parseInt(s_autocount);
			
			if(0.3 <= seed && seed <= 1.0)  {
				for(int ctr = 0; ctr < autocount; ctr++)  {
					p.datacreation(seed);
					d.repaint();
					i.setString();
				}
			}else JOptionPane.showMessageDialog(null, "B社の値は30以上100以下にして下さい");
		}	
	}

}

class InfoPanel extends Panel {
	private Control p;
	Label l_A, l_B, l_ALL, l_winA, l_winB;
	
	public InfoPanel(Control ca) {
		this.p = ca;
		
		setForeground(Color.yellow);
		setBackground(new Color(0, 0, 102));
		
		prepareLabels();
	}
	
	public void prepareLabels() {
		l_A = new Label(String.format("A社: %d", p.countA));
		add(l_A);
		
		l_B = new Label(String.format("B社: %d", p.countB));
		add(l_B);
		
		l_ALL = new Label(String.format("引き分け: %d", p.countDRAW));
		add(l_ALL);
		
		l_winA = new Label(String.format("A累計: %d", p.winA));
		add(l_winA);
		
		l_winB = new Label(String.format("B累計: %d", p.winB));
		add(l_winB);
	}
	
	public void setString() {
		l_A.setText(String.format("A社: %d", p.countA));
		l_B.setText(String.format("B社: %d", p.countB));
		l_ALL.setText(String.format("引き分け: %d", p.countDRAW));
		l_winA.setText(String.format("A累計: %d", p.winA));
		l_winB.setText(String.format("B累計: %d", p.winB));
	}
}
