import java.util.Arrays;
import java.util.Random;

public class KohonenNeuralNetwork {
    private int sizeOfFrame;
    private Neuron[] neurons;
    private double learnStep;
    Random random = new Random();

    public KohonenNeuralNetwork(int sizeOfFrame, int numberOfNeurons, double learnStep) {
        this.sizeOfFrame = sizeOfFrame;
        this.learnStep = learnStep;
        initializeNeurons(numberOfNeurons);
    }

    public void trainNetwork(int trainingEpochAmount, int[][] imgArray) {
        for (int i = 0; i < neurons.length; i++) {
            System.out.println("NEURON " + i + ": " + Arrays.toString(neurons[i].getWeights()));
        }
        for (int i = 0; i < trainingEpochAmount; i++) {
            int[] beginningOfFrame = selectRandomBeginningOfFrame(imgArray.length, sizeOfFrame);
            int[] frame = getFrame(beginningOfFrame, imgArray, sizeOfFrame);
            double squareOfSumOfVector = getSquareOfSumOfVector(frame);
            double[] normalizedFrame = normalizeFrame(frame, squareOfSumOfVector);
            train(normalizedFrame);
        }
        for (int i = 0; i < neurons.length; i++) {
            System.out.println("NEURON " + i + ": " + Arrays.toString(neurons[i].getWeights()));
        }
    }

    private void train(double[] frame) {
        int bestNeuron = chooseBestNeuron(frame);
        changeWeights(frame, bestNeuron);
    }

    private void changeWeights(double[] frame, int bestNeuron) {
        double[] weights = neurons[bestNeuron].getWeights();
        for (int i = 0; i < weights.length; i++) {
            weights[i] = weights[i] + learnStep * (frame[i] - weights[i]);
        }
        normalizeWeights(weights);
        neurons[bestNeuron].setWeights(weights);
    }

    private void normalizeWeights(double[] weights) {
        double squareOfSumOfVector = getSquareOfSumOfVector(weights);
        for (int i = 0; i < weights.length; i++) {
            if (squareOfSumOfVector == 0) {
                weights[i] = 0;
            } else {
                weights[i] = weights[i] / squareOfSumOfVector;
            }
        }
    }

    private int chooseBestNeuron(double[] frame) {
        int bestNeuron = -1;
        double closestDistance = Double.MAX_VALUE;
        for (int i = 0; i < neurons.length; i++) {
            neurons[i].setInputs(frame);
            double distance = neurons[i].countOutput();
            if (distance < closestDistance) {
                closestDistance = distance;
                bestNeuron = i;
            }
        }
        return bestNeuron;
    }

    private double[] normalizeFrame(int[] frame, double squareOfSumOfVector) {
        double[] normalizedFrame = new double[frame.length];
        for (int i = 0; i < frame.length; i++) {
            if (squareOfSumOfVector == 0) {
                normalizedFrame[i] = 0;
            } else {
                normalizedFrame[i] = frame[i] / squareOfSumOfVector;
            }
        }
        return normalizedFrame;
    }


    private double getSquareOfSumOfVector(int[] frame) {
        double sum = 0;
        for (int i = 0; i < frame.length; i++) {
            sum += Math.pow((frame[i]), 2);
        }
        return Math.sqrt(sum);
    }

    private double getSquareOfSumOfVector(double[] frame) {
        double sum = 0;
        for (int i = 0; i < frame.length; i++) {
            sum += Math.pow((frame[i]), 2);
        }
        return Math.sqrt(sum);
    }

    private int[] getFrame(int[] begginingOfFrame, int[][] imgArray, int sizeOfFrame) {
        int[] frame = new int[sizeOfFrame * sizeOfFrame];
        int counter = 0;
        for (int i = 0; i < sizeOfFrame; i++) {
            for (int j = 0; j < sizeOfFrame; j++) {
                frame[counter] = imgArray[begginingOfFrame[0] + i][begginingOfFrame[1] + j];
                counter++;
            }
        }
        return frame;
    }

    private int[] selectRandomBeginningOfFrame(int imgArraySize, int sizeOfFrame) {
        int[] result = new int[2];
        result[0] = random.nextInt(imgArraySize - sizeOfFrame);
        result[1] = random.nextInt(imgArraySize - sizeOfFrame);
        return result;
    }

    private void initializeNeurons(int numberOfNeurons) {
        neurons = new Neuron[numberOfNeurons];
        for (int i = 0; i < neurons.length; i++) {
            neurons[i] = new Neuron(sizeOfFrame * sizeOfFrame);
        }
    }

    public CompressionResult compress(int[][] imgArr) {
        int totalFrames = (imgArr.length * imgArr[0].length) / (sizeOfFrame * sizeOfFrame);
        int framesInOneRow = imgArr.length / sizeOfFrame;
        int counter = 0;
        CompressionResult compressionResult = new CompressionResult(totalFrames);
        compressionResult.setSizeOfFrame(sizeOfFrame);
        for (int i = 0; i < framesInOneRow; i++) {
            for (int j = 0; j < framesInOneRow; j++) {
                int[] beginningOfframe = {sizeOfFrame * i, sizeOfFrame * j};
                int[] frame = getFrame(beginningOfframe, imgArr, sizeOfFrame);
                double squareOfSumOfVector = getSquareOfSumOfVector(frame);
                double[] normalizedFrame = normalizeFrame(frame, squareOfSumOfVector);
                int bestNeuron = chooseBestNeuron(normalizedFrame);
                compressionResult.getNeuronIndexes()[counter] = bestNeuron;
                compressionResult.getLengths()[counter] = squareOfSumOfVector;
                counter++;
            }
        }
        if (counter != totalFrames) {
            throw new IllegalStateException("Counter must be equal to totalFrames after compression");
        }
        for (int i = 0; i < neurons.length; i++) {
            compressionResult.getIndexToWeights().put(i, neurons[i].getWeights());
        }
        return compressionResult;
    }
}