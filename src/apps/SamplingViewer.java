package apps;

import game.SampleSpecs;
import data.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import utils.BitHacking;
import utils.Macro;

//GUI debugger for matching bit keys to the probabilitic samplings
public class SamplingViewer 
{
	private static final int DIM = 10;
	private static final int DELTA = 1;
	private static final SampleSpecs specs = new SampleSpecs(DIM, DIM, DIM, DIM, DELTA);
	private FrequencyTable table;
	private LargeBitsMap largeBitsMap;
	private BitsMap bitsMap;
	private int currIndex = 12;
	private Key key;
	private long smallKey;
	public static void main(String[] args)
	{
		if(specs.isLarge())
		{
			new SamplingViewer(new CompactTable(specs), new CompactLargeBitsMap(specs, 2));
		}
		else
		{
			new SamplingViewer(new FrequencyTable(specs), new BitsMap(specs));
		}
	}
	public SamplingViewer(FrequencyTable table, LargeBitsMap map)
	{
		this.table = table;
		this.largeBitsMap = map;
		init();
	}
	public SamplingViewer(FrequencyTable table, BitsMap map)
	{
		this.table = table;
		this.bitsMap = map;
		init();
	}
	private void init()
	{
		refreshKey();
		createView();
	}
	private void createView()
	{
		JFrame frame = new JFrame();
		JPanel mainPanel = new JPanel(new GridLayout(1, 2));
		JPanel controlPanel = new JPanel();
		frame.getContentPane().add(mainPanel);
		frame.getContentPane().add(controlPanel, BorderLayout.SOUTH);
		DrawKey draw = new DrawKey();
		draw.setMaximumSize(new Dimension(100, 100));
		DrawSmallKey smallDraw = new DrawSmallKey();
		DrawFrequency freq = new DrawFrequency();
		freq.setMaximumSize(new Dimension(100, 100));
		if(specs.isLarge())
		{
			mainPanel.add(draw);
		}
		else
		{
			mainPanel.add(smallDraw);
		}
		mainPanel.add(freq);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.setLocation(new Point(200, 200));
		frame.setVisible(true);
		
		while(true)
		{
			while(true)
			{
				currIndex = new Random().nextInt(1000); //size of bitsmap, ideally
				if(table.numSamples(currIndex) > 10)
				{
					break;
				}
			}
			refreshKey();
			System.out.println(currIndex);
			Macro.sleep(2000);
			System.out.println();
		}
	}
	private void refreshKey()
	{
		if(specs.isLarge())
		{
			for(Key key : largeBitsMap.getMap().keySet())
			{
				if(largeBitsMap.get(key) == currIndex)
				{
					this.key = key;
				}
			}
		}
		else
		{
			for(Long key : bitsMap.getMap().keySet())
			{
				if(bitsMap.get(key) == currIndex)
				{
					smallKey = key;
				}
			}
		}
	}
	class DrawSmallKey extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void paint(Graphics g)
		{
			Graphics2D g2d = (Graphics2D) g;
			int rectWidth = getWidth() / table.getSpecs().getCollectionRows();
			int rectHeight = getHeight() / table.getSpecs().getCollectionCols();
			for(int a = 0; a < table.getSpecs().getCollectionCols(); a ++)
			{
				for(int b = 0; b < table.getSpecs().getCollectionRows(); b ++)
				{
					if(BitHacking.nthBit(smallKey, b * table.getSpecs().getCollectionRows() + a) == 1)
					{
						g2d.setColor(Color.BLACK);
					}
					else
					{
						g2d.setColor(Color.WHITE);
					}
					g2d.fillRect(b * rectWidth, a * rectHeight, (b + 1) * rectWidth, (a + 1) * rectHeight);
				}
			}
			updateUI();
		}
	}
	class DrawKey extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void paint(Graphics g)
		{
			if(key == null) return;
			Graphics2D g2d = (Graphics2D) g;
			int rectWidth = getWidth() / table.getSpecs().getCollectionRows();
			int rectHeight = getHeight() / table.getSpecs().getCollectionCols();
			for(int a = 0; a < table.getSpecs().getCollectionCols(); a ++)
			{
				for(int b = 0; b < table.getSpecs().getCollectionRows(); b ++)
				{
					if(key.print(100, 50).charAt(b * table.getSpecs().getCollectionRows() + a) == '1')
					{
						g2d.setColor(Color.BLACK);
					}
					else
					{
						g2d.setColor(Color.WHITE);
					}
					g2d.fillRect(b * rectWidth, a * rectHeight, (b + 1) * rectWidth, (a + 1) * rectHeight);
				}
			}
			updateUI();
		}
	}
	class DrawFrequency extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void paint(Graphics g)
		{
			Graphics2D g2d = (Graphics2D) g;
			int rectWidth = getWidth() / table.getSpecs().getCollectionRows();
			int rectHeight = getHeight() / table.getSpecs().getCollectionCols();
			for(int a = 0; a < table.getSpecs().getCollectionRows(); a ++)
			{
				for(int b = 0; b < table.getSpecs().getCollectionCols(); b ++)
				{
					float freq = (float) table.get(currIndex, b * table.getSpecs().getCollectionCols() + a);
					if(freq > 1)
					{
						System.out.println();
					}
					g2d.setColor(new Color(1 - freq, 1 - freq, 1 - freq));
					g2d.fillRect(b * rectWidth, a * rectHeight, (b + 1) * rectWidth, (a + 1) * rectHeight);
				}
			}
			updateUI();
		}
	}
}
