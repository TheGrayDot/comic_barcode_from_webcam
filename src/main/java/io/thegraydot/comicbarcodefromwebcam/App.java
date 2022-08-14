package io.thegraydot.comicbarcodefromwebcam;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.WebcamDiscoveryService;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.multi.GenericMultipleBarcodeReader;

public class App {
    public static void main(String[] args) {
        // Configure zxing hints
        // Set barcode formats to only known ones used in comics
        List<BarcodeFormat> possibleFormats = Arrays.asList(
                BarcodeFormat.UPC_A,
                BarcodeFormat.EAN_13,
                BarcodeFormat.UPC_EAN_EXTENSION);

        // Set possible barcode extensions to length 2 or 5 digits
        // int[] possibleExtensions = { 2, 5 };
        int[] possibleExtensions = { 5 };

        // Populate hints variable to pass to scanner
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, possibleFormats);
        hints.put(DecodeHintType.ALLOWED_EAN_EXTENSIONS, possibleExtensions);

        Webcam webcam;
        WebcamDiscoveryService discovery = Webcam.getDiscoveryService();

        List<Webcam> webcams = new ArrayList<Webcam>();
        try {
            webcams = discovery.getWebcams(5, TimeUnit.SECONDS);
            System.out.println("[+] Available webcams:");
            int index = 0;
            for (Webcam webcamFound : webcams) {
                System.out.println("[" + index + "] " + webcamFound.getName());
                index++;
            }

            System.out.print("[+] Enter webcam number:");
            Scanner webcamNameScanner = new Scanner(System.in);
            int webcamNumber = webcamNameScanner.nextInt();
            webcamNameScanner.close();
            webcam = webcams.get(webcamNumber);
        } catch (TimeoutException e) {
            // Handle didn't find camera
            // Currently a hack to just get the default one!
            webcam = Webcam.getDefault();
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize());

        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(true);
        panel.setImageSizeDisplayed(true);
        JFrame window = new JFrame("Test webcam panel");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);

        String previousBarcode = "";

        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Result[] results = null;
            BufferedImage image = null;

            if (webcam.isOpen()) {

                // Skip when no image present
                if ((image = webcam.getImage()) == null) {
                    continue;
                }

                // Read in image
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                // Setup barcode reader
                // Needs to be Multi, to read EAN-5/EAN-2
                MultiFormatReader multiFormatReader = new MultiFormatReader();
                MultipleBarcodeReader reader = new GenericMultipleBarcodeReader(multiFormatReader);

                try {
                    // Print a dot (without break) to see progress
                    System.out.print(".");
                    results = reader.decodeMultiple(bitmap, hints);
                } catch (NotFoundException e) {
                    // Pass, it means there is no barcode found
                }
            }

            if (results != null) {
                // Print an x to see progress (got a result)
                System.out.print("x");

                for (Result result : results) {
                    // Get the base barcode (no extension)
                    String barcode = result.getText();

                    // Get the extension (EAN-2/EAN-5)
                    Map<ResultMetadataType, Object> resultMetadata = result.getResultMetadata();
                    Object extension = resultMetadata.get(ResultMetadataType.UPC_EAN_EXTENSION);
                    String fullBarcode = barcode + extension;
                    
                    if (previousBarcode == fullBarcode) {
                        // Skip if barcode was previously scanned
                        continue;
                    } else {
                        System.out.println("\n" + fullBarcode);

                        // Test code to echo barcode as keystrokes to another app

                        // // Copy barcode string to clipboard
                        // // This is to paste in extenal app
                        // StringSelection stringSelection = new StringSelection(fullBarcode);
                        // Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        // clipboard.setContents(stringSelection, stringSelection);

                        // try {
                        //     Robot robot = new Robot();
                        //     // Press Ctrl + V to paste
                        //     robot.keyPress(KeyEvent.VK_CONTROL);
                        //     robot.keyPress(KeyEvent.VK_V);
                        //     robot.keyRelease(KeyEvent.VK_V);
                        //     robot.keyRelease(KeyEvent.VK_CONTROL);

                        //     // Press Enter for a line break
                        //     robot.keyPress(KeyEvent.VK_ENTER);
                        //     robot.keyRelease(KeyEvent.VK_ENTER);
                        // } catch (AWTException e) {
                        //     // Do something with KeyError
                        // }

                        // Since we found a barcode...
                        // Remember for the next comic and don't print again
                        previousBarcode = fullBarcode;
                    }
                }
            }

        } while (true);

    }
}
