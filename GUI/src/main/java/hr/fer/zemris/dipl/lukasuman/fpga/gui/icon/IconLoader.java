package hr.fer.zemris.dipl.lukasuman.fpga.gui.icon;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class contains methods for loading icons.
 * @author Luka Suman
 * @version 1.0
 */
public class IconLoader {

	/**Location of blue diskette icon.*/
	private static final String BLUE_DISKETTE_LOCATION = "diskette_blue.png";

	/**Location of red diskette icon.*/
	private static final String RED_DISKETTE_LOCATION = "diskette_red.png";

	/**Error message.*/
	private static final String ERR_MSG = "Error while trying to load icon!";

	/**
	 * Loads icon for red diskette.
	 * @return Returns icon for red diskette.
	 * @throws IOException If the icon couldn't be loaded.
	 */
	public static ImageIcon loadRedDisketteIcon() throws IOException {
		return loadIcon(RED_DISKETTE_LOCATION);
	}

	/**
	 * Loads icon for blue diskette.
	 * @return Returns icon for blue diskette.
	 * @throws IOException If the icon couldn't be loaded.
	 */
	public static ImageIcon loadBlueDisketteIcon() throws IOException {
		return loadIcon(BLUE_DISKETTE_LOCATION);
	}

	/**
	 * Loads icon at the specified location.
	 * @param location Location of the icon.
	 * @return Returns icon for red diskette.
	 * @throws IOException If the icon couldn't be loaded.
	 */
	private static ImageIcon loadIcon(String location) throws IOException {
		InputStream is = IconLoader.class.getClassLoader().getResourceAsStream(location);
		if (is == null) {
			throw new IOException(ERR_MSG);
		}
		byte[] buffer = new byte[8192];
		int bytesRead;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		while ((bytesRead = is.read(buffer)) != -1)
		{
			os.write(buffer, 0, bytesRead);
		}
		is.close();
		byte[] bytes = os.toByteArray();
		os.close();

		ImageIcon loadedIcon = new ImageIcon(bytes);
		Image scaledImage = getScaledImage(loadedIcon.getImage(),
				GUIConstants.DEFAULT_ICON_SIZE.width, GUIConstants.DEFAULT_ICON_SIZE.height);
		return new ImageIcon(scaledImage);
	}

	private static Image getScaledImage(Image srcImg, int w, int h){
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return resizedImg;
	}
}
