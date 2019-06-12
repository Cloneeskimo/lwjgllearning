package engine.gameitem;

import engine.graphics.Mesh;
import engine.graphics.anim.AnimatedFrame;
import org.joml.Matrix4f;

import java.util.List;

public class AnimGameItem extends GameItem {

    //Data
    private int currentFrame;
    private List<AnimatedFrame> frames;
    private List<Matrix4f> invJointMatrices;

    //Constructor
    public AnimGameItem(Mesh[] meshes, List<AnimatedFrame> frames, List<Matrix4f> invJointMatrices) {
        super(meshes);
        this.frames = frames;
        this.invJointMatrices = invJointMatrices;
        this.currentFrame = 0;
    }

    //Accessors
    public List<AnimatedFrame> getFrames() { return this.frames; }
    public List<Matrix4f> getInvJointMatrices() { return this.invJointMatrices; }
    public AnimatedFrame getCurrentFrame() { return this.frames.get(currentFrame); }
    public AnimatedFrame getNextFrame() {
        int nf = this.currentFrame + 1;
        if (nf >= this.frames.size()) nf = 0;
        return this.frames.get(nf);
    }

    //Mutators
    public void setFrames(List<AnimatedFrame> frames) { this.frames = frames; }

    //Frame Cycling Method
    public void nextFrame() {
        this.currentFrame++;
        if (this.currentFrame >= this.frames.size()) this.currentFrame = 0;
    }
}
