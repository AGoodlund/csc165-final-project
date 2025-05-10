package a2;
import tage.GameObject;
import tage.TextureImage;
import tage.ObjShape;
import tage.shapes.AnimatedShape;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.Matrix4f;
//import tage.networking.Message.CharacterType;
import tage.networking.Message.MessageType;

public class ChangeCharacterAction extends AbstractInputAction{
    private GameObject obj;
    private Character diver, dol, fish, dol_gay;
    private Character[] cast;
    private MessageType[] names = {MessageType.DIVER, MessageType.DOL, MessageType.ENEMY, MessageType.GAY_DOL};
//    private CharacterType[] names = {CharacterType.DIVER, CharacterType.DOL, CharacterType.ENEMY, CharacterType.GAY_DOL};
    private ProtocolClient protClient;
    private int tracker = 1;

    private Matrix4f m = new Matrix4f();

    public ChangeCharacterAction(GameObject avatar, ProtocolClient p){
        obj = avatar;
        protClient = p;
        diver = new Character();
        dol = new Character();
        fish = new Character();
        dol_gay = new Character();
        cast = new Character[] {diver, dol, fish, dol_gay};
    }

    public void addTextures(TextureImage diverX, TextureImage dolX, TextureImage pufferX){
        diver.addTexture(diverX);
        dol.addTexture(dolX);
        fish.addTexture(pufferX);
        dol_gay.addTexture(null);
    }
    public void addShapes(ObjShape diverS, ObjShape dolS, ObjShape pufferS){
        diver.addShape(diverS);
        dol.addShape(dolS);
        fish.addShape(pufferS);
        dol_gay.addShape(dolS);
    }
    public void addSizes(Matrix4f diverScale, Matrix4f dolScale, Matrix4f pufferScale){
        diver.addSize(diverScale);
        dol.addSize(dolScale);
        fish.addSize(pufferScale);
        dol_gay.addSize(dolScale);
    }

    @Override
    public void performAction(float time, Event e){
        cast[tracker].changeCharacter(obj);
System.out.println("sending NPC_CHANGE with " + names[tracker]);
        protClient.changeAvatar(names[tracker]); //swap the ghost to the same setup

        tracker++;
        tracker = tracker % cast.length;
    }


    private class Character{
        public TextureImage skin;
        public ObjShape shape;
        public Matrix4f size = new Matrix4f();

        public void addTexture(TextureImage X){ skin = X; }
        public void addShape(ObjShape S){ shape = S; }
        public void addSize(Matrix4f scale){ size.set(scale); }
        public TextureImage getTexture(){ return skin;}
        public ObjShape getShape(){ return shape; }
        public void getScale(Matrix4f m){ m.set(size); }

        public void changeCharacter(GameObject g){ //make the GameObject look like this character
            g.setTextureImage(skin);
            g.setShape(shape);
            g.setLocalScale(size);
        }
    }   
}
