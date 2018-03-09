package ru.flametaichou.levelup.Entity;

import cpw.mods.fml.common.registry.IThrowableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

public class EntityBonusArrow extends EntityArrow implements IThrowableEntity
{
    Entity thower;

    public EntityBonusArrow(World world, EntityLivingBase archer, float f)
    {
        super(world, archer, f);
        setThrower(archer);
    }

    @Override
    public Entity getThrower() {
        return this.thower;
    }

    @Override
    public void setThrower(Entity entity) {
        this.thower = entity;
    }


    @Override
    public void onUpdate() {
        super.onUpdate();
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle("magicCrit", this.posX + this.motionX * (double)i / 4.0D, this.posY + this.motionY * (double)i / 4.0D, this.posZ + this.motionZ * (double)i / 4.0D, -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
}
