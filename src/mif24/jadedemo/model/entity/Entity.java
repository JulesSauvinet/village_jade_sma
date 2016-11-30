package mif24.jadedemo.model.entity;

import mif24.jadedemo.model.descriptor.EntityDescriptor;

import java.io.Serializable;

/**
 * Created by Jules on 18/05/2016.
 */
public class Entity implements Serializable{

    public EntityDescriptor entityDescriptor;

    public int X = -1, Y = -1;
    public int height;
    public int width;
    public int pos=-1;

    public Entity(int height, int width, EntityDescriptor entityDescriptor){
        this.entityDescriptor = entityDescriptor;
        this.height=height;
        this.width=width;
    }

    public Entity() {
    }
}
