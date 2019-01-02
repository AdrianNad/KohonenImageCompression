import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompressionResult {
    private int[] neuronIndexes;
    private double[] lengths;
    private Map<Integer, double[]> indexToWeights = new HashMap<>();
    private int sizeOfFrame;

    public int getSizeOfFrame() {
        return sizeOfFrame;
    }

    public void setSizeOfFrame(int sizeOfFrame) {
        this.sizeOfFrame = sizeOfFrame;
    }

    public CompressionResult(int totalFrames) {
        neuronIndexes = new int[totalFrames];
        lengths = new double[totalFrames];
    }

    public int[] getNeuronIndexes() {
        return neuronIndexes;
    }

    public void setNeuronIndexes(int[] neuronIndexes) {
        this.neuronIndexes = neuronIndexes;
    }

    public double[] getLengths() {
        return lengths;
    }

    public void setLengths(double[] lengths) {
        this.lengths = lengths;
    }

    public Map<Integer, double[]> getIndexToWeights() {
        return indexToWeights;
    }

    public void setIndexToWeights(Map<Integer, double[]> indexToWeights) {
        this.indexToWeights = indexToWeights;
    }
}
