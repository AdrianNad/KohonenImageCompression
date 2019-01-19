import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        int[][] imgArr = loadImage("src/images/boat.png");
        int sizeOfFrame = 2;
        int numberOfNeurons = 8;
        KohonenNeuralNetwork network = new KohonenNeuralNetwork(sizeOfFrame, numberOfNeurons, 0.01);
        network.trainNetwork(150000, imgArr);
        CompressionResult compressionResult = network.compress(imgArr);
        int[][] decompressedImage = decompress(compressionResult);
        saveImage(decompressedImage, "src/images/saved.png");
        double PSNR = countPSNR(imgArr, decompressedImage);
        System.out.println("PSNR: " + PSNR + " dB");
        double compressionFactor = countCompressionFactor(imgArr, sizeOfFrame, numberOfNeurons, 8);
        System.out.println("Compression factor: " + compressionFactor);
    }

    private static double countCompressionFactor(int[][] imgArr, int sizeOfFrame, int numberOfNeurons, int bitsPerValue) {
        int numberOfPixels = imgArr.length * imgArr[0].length;
        double b1 = numberOfPixels * 8;
        int numberOfFrames = numberOfPixels / sizeOfFrame;
        int pixelsPerFrame = sizeOfFrame * sizeOfFrame;
        double b2 = numberOfFrames * (Math.ceil(log2(numberOfNeurons)) + bitsPerValue) + (numberOfNeurons * pixelsPerFrame * bitsPerValue);
        return b2/b1;
    }

    private static double log2(int n)
    {
        return (Math.log(n) / Math.log(2));
    }

    private static double countPSNR(int[][] oldImage, int[][] newImage) {
        double MSE = countMSE(oldImage, newImage);
        return 10 * Math.log10(Math.pow(255,2)/MSE);
    }

    private static double countMSE(int[][] oldImage, int[][] newImage) {
        int sizeOfPicture = oldImage.length;
        float pixels = sizeOfPicture * sizeOfPicture;
        int sum = 0;
        for (int i = 0; i < sizeOfPicture; i++) {
            for (int j = 0; j < sizeOfPicture; j++) {
                sum += Math.pow(oldImage[i][j]-newImage[i][j],2);
            }
        }
        return 1/pixels * sum;
    }

    private static int[][] decompress(CompressionResult compressionResult) {

        int sizeOfFrame = compressionResult.getSizeOfFrame();
        int sizeOfImage = (int) Math.sqrt(compressionResult.getNeuronIndexes().length * (sizeOfFrame * sizeOfFrame));
        int[][] decompressionResult = new int[sizeOfImage][sizeOfImage];
        int framesInOneRow = decompressionResult.length / sizeOfFrame;
        int counter = 0;
        for (int i = 0; i < framesInOneRow; i++) {
            for (int j = 0; j < framesInOneRow; j++) {
                double[] weights = compressionResult.getIndexToWeights().get(compressionResult.getNeuronIndexes()[counter]);
                int[] decompressedFrame = decompressFrame(weights, compressionResult.getLengths()[counter]);
                putFrame(decompressionResult, decompressedFrame, i * sizeOfFrame, j * sizeOfFrame, sizeOfFrame);
                counter++;
            }
        }
        return decompressionResult;
    }

    private static void putFrame(int[][] decompressionResult, int[] decompressedFrame, int i, int j, int sizeOfFrame) {
        int counter = 0;
        for (int k = 0; k < sizeOfFrame; k++) {
            for (int l = 0; l < sizeOfFrame; l++) {
                decompressionResult[i+k][j+l] = decompressedFrame[counter];
                counter++;
            }
        }
    }

    private static int[] decompressFrame(double[] weights, double length) {
        double[] decompressedFrame = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            decompressedFrame[i] = weights[i] * length;
        }
        return frameToInt(decompressedFrame);
    }

    private static int[] frameToInt(double[] decompressedFrame) {
        int[] intFrame = new int[decompressedFrame.length];
        for (int i = 0; i < decompressedFrame.length; i++) {
            intFrame[i] = (int) decompressedFrame[i];
        }
        return intFrame;
    }

    private static void saveImage(int[][] imgArr, String path) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(imgArr.length, imgArr[0].length, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster wr = bufferedImage.getRaster();
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                wr.setSample(i, j, 0, imgArr[i][j]);
            }
        }

        File file1 = new File(path);
        ImageIO.write(bufferedImage, "PNG", file1);
    }

    private static int[][] loadImage(String path) throws IOException {
        File file = new File(path);
        BufferedImage img = ImageIO.read(file);
        int width = img.getWidth();
        int height = img.getHeight();
        int[][] imgArr = new int[width][height];
        Raster raster = img.getData();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                imgArr[i][j] = raster.getSample(i, j, 0);
            }
        }
        return imgArr;
    }
}
