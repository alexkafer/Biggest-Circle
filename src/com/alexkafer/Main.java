package com.alexkafer;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class Main {
	

	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat original = new Mat();

		MatWindow window = new MatWindow("Camera");
		MatWindow threshWindow = new MatWindow("Thresh");

		VideoCapture camera = new VideoCapture(0);

		JFrame jFrame = new JFrame("Options");
		jFrame.setSize(200, 200);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setLayout(new FlowLayout());

		JPanel panel = new JPanel();

		JSlider hueSlider = new JSlider(0, 255, 178);
		panel.add(hueSlider);

		JSlider satSlider = new JSlider(0, 255, 255);
		panel.add(satSlider);

		JSlider valSlider = new JSlider(10, 255, 247);
		panel.add(valSlider);

		JSlider tolSlider = new JSlider(0, 255, 79);
		panel.add(tolSlider);

		jFrame.setContentPane(panel);
		jFrame.setVisible(true);

		while (true) {
			
			if (!camera.read(original))
				continue;
			
			Mat threshImage = new Mat();

			Imgproc.cvtColor(original, threshImage, Imgproc.COLOR_RGB2HSV);

			int hue = hueSlider.getValue();
			int satu = satSlider.getValue();
			int valu = valSlider.getValue();
			int tol = tolSlider.getValue();

			Core.inRange(
					threshImage,
					new Scalar(Math.max(hue - tol, 0), Math.max(satu - tol, 0),
							Math.max(valu - tol, 0)),
					new Scalar(Math.min(hue + tol, 179), Math.min(satu + tol,
							255), Math.min(valu + tol, 255)), threshImage);

			threshWindow.setImage(threshImage);
			

			List<MatOfPoint> particles = new ArrayList<MatOfPoint>();
			

			Imgproc.findContours(threshImage, particles, new Mat(),
					Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			
			Rect biggest = null;
			int biggestArea = 0;
			
			for (int i = particles.size()-1; i > 0; i--) {
				MatOfPoint contour = particles.get(i);
				int area = contour.width() * contour.height();
				if (area > biggestArea) {
					//yellowTotes.remove(i);
					biggest = Imgproc.boundingRect(contour);
					biggestArea = area;
				} else {
					particles.remove(i);
				}
				
			}
			
			if (biggest != null) {

				Point center = new Point(
						(biggest.tl().x + biggest.br().x) / 2,
						(biggest.tl().y + biggest.br().y) / 2);
		
				Core.circle(original, center, biggest.width/2, new Scalar(0, 255, 255));
		
			}

			// Update the image on the window
			window.setImage(original);

		}
	}
}
