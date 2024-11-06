//package controller;
//import org.bytedeco.javacv.OpenCVFrameGrabber;
//import org.bytedeco.javacv.Frame;
//import org.bytedeco.javacv.Java2DFrameConverter;
//import java.awt.image.BufferedImage;
//import javax.imageio.ImageIO;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//
//public class WebcamManager {
//    private OpenCVFrameGrabber grabber;
//
//    public WebcamManager() throws Exception {
//        grabber = new OpenCVFrameGrabber(0); // default camera
//        grabber.start();
//    }
//
//    public byte[] captureFrame() throws IOException {
//        Frame frame = grabber.grab();
//        BufferedImage image = new Java2DFrameConverter().convert(frame);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ImageIO.write(image, "jpg", baos); // Encode as JPG
//        return baos.toByteArray();
//    }
//
//    public void stop() throws Exception {
//        grabber.stop();
//    }
//}
