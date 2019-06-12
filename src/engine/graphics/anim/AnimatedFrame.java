package engine.graphics.anim;

import org.joml.Matrix4f;

import java.util.Arrays;

public class AnimatedFrame {

    //Static Data
    public static final int MAX_JOINTS = 150;
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();

    //Data
    private final Matrix4f[] localJointMatrices;
    private final Matrix4f[] jointMatrices;

    //Constructor
    public AnimatedFrame() {
        this.localJointMatrices = new Matrix4f[MAX_JOINTS];
        Arrays.fill(this.localJointMatrices, IDENTITY_MATRIX);
        this.jointMatrices = new Matrix4f[MAX_JOINTS];
        Arrays.fill(this.jointMatrices, IDENTITY_MATRIX);
    }

    //Accessors
    public Matrix4f[] getLocalJointMatrices() { return this.localJointMatrices; }
    public Matrix4f[] getJointMatrices() { return this.jointMatrices; }

    //Matrix Mutator
    public void setMatrix(int pos, Matrix4f localJointMatrix, Matrix4f invJointMatrix) {
        this.localJointMatrices[pos] = localJointMatrix;
        Matrix4f mat = new Matrix4f(localJointMatrix);
        mat.mul(invJointMatrix);
        jointMatrices[pos] = mat;
    }
}
