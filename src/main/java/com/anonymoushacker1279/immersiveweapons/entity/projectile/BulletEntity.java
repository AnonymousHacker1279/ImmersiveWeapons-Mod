package com.anonymoushacker1279.immersiveweapons.entity.projectile;

import com.anonymoushacker1279.immersiveweapons.init.DeferredRegistryHandler;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BulletEntity {
	
	private static float getRandomNumber(float min, float max) {
	    return (float) ((Math.random() * (max - min)) + min);
	}
	
	public static class CopperBulletEntity extends AbstractArrowEntity {
		private final Item referenceItem;

	    @SuppressWarnings("unchecked")
	    public CopperBulletEntity(EntityType<?> type, World world) {
	        super((EntityType<? extends AbstractArrowEntity>) type, world);
	        this.referenceItem = DeferredRegistryHandler.COPPER_MUSKET_BALL.get();
	    }
	    
	    public CopperBulletEntity(LivingEntity shooter, World world, Item referenceItemIn) {
	        super(DeferredRegistryHandler.COPPER_BULLET_ENTITY.get(), shooter, world);
	        this.referenceItem = referenceItemIn;
	    }

	    public CopperBulletEntity(World worldIn, double x, double y, double z) {
	    	super(DeferredRegistryHandler.COPPER_BULLET_ENTITY.get(), x, y, z, worldIn);
			this.referenceItem = DeferredRegistryHandler.COPPER_MUSKET_BALL.get();
	     }

		@Override
	    public ItemStack getArrowStack() {
	        return new ItemStack(this.referenceItem);
	    }
		
		@Override
		public IPacket<?> createSpawnPacket() {
			return NetworkHooks.getEntitySpawningPacket(this);
		}
		
		@Override
		public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
			Vector3d vector3d = (new Vector3d(x, y, z)).normalize().add(this.rand.nextGaussian() * 0.0025F * (getRandomNumber(0.2f, 1.1f)), 0.00025F * (getRandomNumber(0.2f, 1.1f)), this.rand.nextGaussian() * 0.0025F).scale(velocity);
		    this.setMotion(vector3d);
		    float f = MathHelper.sqrt(horizontalMag(vector3d));
		    this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		    this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		    this.prevRotationYaw = this.rotationYaw;
		    this.prevRotationPitch = this.rotationPitch;
		}
		
		private BlockState inBlockState;
		
		private boolean func_234593_u_() {
		      return this.inGround && this.world.hasNoCollisions((new AxisAlignedBB(this.getPositionVec(), this.getPositionVec())).grow(0.06D));
		}
		private void func_234594_z_() {
		      this.inGround = false;
		      Vector3d vector3d = this.getMotion();
		      this.setMotion(vector3d.mul(this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F));
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void tick() {
		      super.tick();
		      boolean flag = this.getNoClip();
		      Vector3d vector3d = this.getMotion();
		      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
		         float f = MathHelper.sqrt(horizontalMag(vector3d));
		         this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		         this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		         this.prevRotationYaw = this.rotationYaw;
		         this.prevRotationPitch = this.rotationPitch;
		      }

		      BlockPos blockpos = this.getPosition();
		      BlockState blockstate = this.world.getBlockState(blockpos);
		      if (!blockstate.isAir(this.world, blockpos) && !flag) {
		         VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
		         if (!voxelshape.isEmpty()) {
		            Vector3d vector3d1 = this.getPositionVec();

		            for(AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList()) {
		               if (axisalignedbb.offset(blockpos).contains(vector3d1)) {
		                  this.inGround = true;
		                  break;
		               }
		            }
		         }
		      }

		      if (this.arrowShake > 0) {
		         --this.arrowShake;
		      }

		      if (this.isWet()) {
		         this.extinguish();
		      }

		      if (this.inGround && !flag) {
		         if (this.inBlockState != blockstate && this.func_234593_u_()) {
		            this.func_234594_z_();
		         } else if (!this.world.isRemote) {
		            this.func_225516_i_();
		         }

		         ++this.timeInGround;
		      } else {
		         this.timeInGround = 0;
		         Vector3d vector3d2 = this.getPositionVec();
		         Vector3d vector3d3 = vector3d2.add(vector3d);
		         RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vector3d2, vector3d3, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
		         if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
		            vector3d3 = raytraceresult.getHitVec();
		         }

		         while(!this.removed) {
		            EntityRayTraceResult entityraytraceresult = this.rayTraceEntities(vector3d2, vector3d3);
		            if (entityraytraceresult != null) {
		               raytraceresult = entityraytraceresult;
		            }

		            if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
		               Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
		               Entity entity1 = this.func_234616_v_();
		               if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canAttackPlayer((PlayerEntity)entity)) {
		                  raytraceresult = null;
		                  entityraytraceresult = null;
		               }
		            }

		            if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
		               this.onImpact(raytraceresult);
		               this.isAirBorne = true;
		            }

		            if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
		               break;
		            }

		            raytraceresult = null;
		         }

		         vector3d = this.getMotion();
		         double d3 = vector3d.x;
		         double d4 = vector3d.y;
		         double d0 = vector3d.z;
		         if (this.getIsCritical()) {
		            for(int i = 0; i < 4; ++i) {
		               this.world.addParticle(ParticleTypes.CRIT, this.getPosX() + d3 * i / 4.0D, this.getPosY() + d4 * i / 4.0D, this.getPosZ() + d0 * i / 4.0D, -d3, -d4 + 0.2D, -d0);
		            }
		         }

		         double d5 = this.getPosX() + d3;
		         double d1 = this.getPosY() + d4;
		         double d2 = this.getPosZ() + d0;
		         float f1 = MathHelper.sqrt(horizontalMag(vector3d));
		         if (flag) {
		            this.rotationYaw = (float)(MathHelper.atan2(-d3, -d0) * (180F / (float)Math.PI));
		         } else {
		            this.rotationYaw = (float)(MathHelper.atan2(d3, d0) * (180F / (float)Math.PI));
		         }

		         this.rotationPitch = (float)(MathHelper.atan2(d4, f1) * (180F / (float)Math.PI));
		         this.rotationPitch = func_234614_e_(this.prevRotationPitch, this.rotationPitch);
		         this.rotationYaw = func_234614_e_(this.prevRotationYaw, this.rotationYaw);
		         float f2 = 0.99F;
		         if (this.isInWater()) {
		            for(int j = 0; j < 4; ++j) {
		               this.world.addParticle(ParticleTypes.BUBBLE, d5 - d3 * 0.25D, d1 - d4 * 0.25D, d2 - d0 * 0.25D, d3, d4, d0);
		            }

		            f2 = this.getWaterDrag();
		         }

		         this.setMotion(vector3d.scale(f2));
		         if (!this.hasNoGravity() && !flag) {
		            Vector3d vector3d4 = this.getMotion();
		            this.setMotion(vector3d4.x, vector3d4.y + 0.0355d, vector3d4.z);
		         }

		         this.setPosition(d5, d1, d2);
		         this.doBlockCollisions();
		      }
		}
	}

	public static class WoodBulletEntity extends AbstractArrowEntity {
		private final Item referenceItem;

	    @SuppressWarnings("unchecked")
	    public WoodBulletEntity(EntityType<?> type, World world) {
	        super((EntityType<? extends AbstractArrowEntity>) type, world);
	        this.referenceItem = DeferredRegistryHandler.WOOD_MUSKET_BALL.get();
	    }
	    
	    public WoodBulletEntity(LivingEntity shooter, World world, Item referenceItemIn) {
	        super(DeferredRegistryHandler.WOOD_BULLET_ENTITY.get(), shooter, world);
	        this.referenceItem = referenceItemIn;
	    }

	    public WoodBulletEntity(World worldIn, double x, double y, double z) {
	    	super(DeferredRegistryHandler.WOOD_BULLET_ENTITY.get(), x, y, z, worldIn);
			this.referenceItem = DeferredRegistryHandler.WOOD_MUSKET_BALL.get();
	     }

		@Override
	    public ItemStack getArrowStack() {
	        return new ItemStack(this.referenceItem);
	    }
		
		@Override
		public IPacket<?> createSpawnPacket() {
			return NetworkHooks.getEntitySpawningPacket(this);
		}
		
		@Override
		public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
			Vector3d vector3d = (new Vector3d(x, y, z)).normalize().add(this.rand.nextGaussian() * 0.0075F * (getRandomNumber(3.2f, 5.1f)), -0.0095F * (getRandomNumber(3.2f, 5.1f)), this.rand.nextGaussian() * 0.0075F).scale(velocity);
		    this.setMotion(vector3d);
		    float f = MathHelper.sqrt(horizontalMag(vector3d));
		    this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		    this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		    this.prevRotationYaw = this.rotationYaw;
		    this.prevRotationPitch = this.rotationPitch;
		}
		
		private BlockState inBlockState;
		
		private boolean func_234593_u_() {
		      return this.inGround && this.world.hasNoCollisions((new AxisAlignedBB(this.getPositionVec(), this.getPositionVec())).grow(0.06D));
		}
		private void func_234594_z_() {
		      this.inGround = false;
		      Vector3d vector3d = this.getMotion();
		      this.setMotion(vector3d.mul(this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F));
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void tick() {
		      super.tick();
		      boolean flag = this.getNoClip();
		      Vector3d vector3d = this.getMotion();
		      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
		         float f = MathHelper.sqrt(horizontalMag(vector3d));
		         this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		         this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		         this.prevRotationYaw = this.rotationYaw;
		         this.prevRotationPitch = this.rotationPitch;
		      }

		      BlockPos blockpos = this.getPosition();
		      BlockState blockstate = this.world.getBlockState(blockpos);
		      if (!blockstate.isAir(this.world, blockpos) && !flag) {
		         VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
		         if (!voxelshape.isEmpty()) {
		            Vector3d vector3d1 = this.getPositionVec();

		            for(AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList()) {
		               if (axisalignedbb.offset(blockpos).contains(vector3d1)) {
		                  this.inGround = true;
		                  break;
		               }
		            }
		         }
		      }

		      if (this.arrowShake > 0) {
		         --this.arrowShake;
		      }

		      if (this.isWet()) {
		         this.extinguish();
		      }

		      if (this.inGround && !flag) {
		         if (this.inBlockState != blockstate && this.func_234593_u_()) {
		            this.func_234594_z_();
		         } else if (!this.world.isRemote) {
		            this.func_225516_i_();
		         }

		         ++this.timeInGround;
		      } else {
		         this.timeInGround = 0;
		         Vector3d vector3d2 = this.getPositionVec();
		         Vector3d vector3d3 = vector3d2.add(vector3d);
		         RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vector3d2, vector3d3, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
		         if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
		            vector3d3 = raytraceresult.getHitVec();
		         }

		         while(!this.removed) {
		            EntityRayTraceResult entityraytraceresult = this.rayTraceEntities(vector3d2, vector3d3);
		            if (entityraytraceresult != null) {
		               raytraceresult = entityraytraceresult;
		            }

		            if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
		               Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
		               Entity entity1 = this.func_234616_v_();
		               if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canAttackPlayer((PlayerEntity)entity)) {
		                  raytraceresult = null;
		                  entityraytraceresult = null;
		               }
		            }

		            if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
		               this.onImpact(raytraceresult);
		               this.isAirBorne = true;
		            }

		            if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
		               break;
		            }

		            raytraceresult = null;
		         }

		         vector3d = this.getMotion();
		         double d3 = vector3d.x;
		         double d4 = vector3d.y;
		         double d0 = vector3d.z;
		         if (this.getIsCritical()) {
		            for(int i = 0; i < 4; ++i) {
		               this.world.addParticle(ParticleTypes.CRIT, this.getPosX() + d3 * i / 4.0D, this.getPosY() + d4 * i / 4.0D, this.getPosZ() + d0 * i / 4.0D, -d3, -d4 + 0.2D, -d0);
		            }
		         }

		         double d5 = this.getPosX() + d3;
		         double d1 = this.getPosY() + d4;
		         double d2 = this.getPosZ() + d0;
		         float f1 = MathHelper.sqrt(horizontalMag(vector3d));
		         if (flag) {
		            this.rotationYaw = (float)(MathHelper.atan2(-d3, -d0) * (180F / (float)Math.PI));
		         } else {
		            this.rotationYaw = (float)(MathHelper.atan2(d3, d0) * (180F / (float)Math.PI));
		         }

		         this.rotationPitch = (float)(MathHelper.atan2(d4, f1) * (180F / (float)Math.PI));
		         this.rotationPitch = func_234614_e_(this.prevRotationPitch, this.rotationPitch);
		         this.rotationYaw = func_234614_e_(this.prevRotationYaw, this.rotationYaw);
		         float f2 = 0.99F;
		         if (this.isInWater()) {
		            for(int j = 0; j < 4; ++j) {
		               this.world.addParticle(ParticleTypes.BUBBLE, d5 - d3 * 0.25D, d1 - d4 * 0.25D, d2 - d0 * 0.25D, d3, d4, d0);
		            }

		            f2 = this.getWaterDrag();
		         }

		         this.setMotion(vector3d.scale(f2));
		         if (!this.hasNoGravity() && !flag) {
		            Vector3d vector3d4 = this.getMotion();
		            this.setMotion(vector3d4.x, vector3d4.y + 0.055d, vector3d4.z);
		         }

		         this.setPosition(d5, d1, d2);
		         this.doBlockCollisions();
		      }
		}
	}
	
	public static class StoneBulletEntity extends AbstractArrowEntity {
		private final Item referenceItem;

	    @SuppressWarnings("unchecked")
	    public StoneBulletEntity(EntityType<?> type, World world) {
	        super((EntityType<? extends AbstractArrowEntity>) type, world);
	        this.referenceItem = DeferredRegistryHandler.STONE_MUSKET_BALL.get();
	    }
	    
	    public StoneBulletEntity(LivingEntity shooter, World world, Item referenceItemIn) {
	        super(DeferredRegistryHandler.STONE_BULLET_ENTITY.get(), shooter, world);
	        this.referenceItem = referenceItemIn;
	    }

	    public StoneBulletEntity(World worldIn, double x, double y, double z) {
	    	super(DeferredRegistryHandler.STONE_BULLET_ENTITY.get(), x, y, z, worldIn);
			this.referenceItem = DeferredRegistryHandler.STONE_MUSKET_BALL.get();
	     }

		@Override
	    public ItemStack getArrowStack() {
	        return new ItemStack(this.referenceItem);
	    }
		
		@Override
		public IPacket<?> createSpawnPacket() {
			return NetworkHooks.getEntitySpawningPacket(this);
		}
		
		@Override
		public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
			Vector3d vector3d = (new Vector3d(x, y, z)).normalize().add(this.rand.nextGaussian() * 0.0075F * (getRandomNumber(2.4f, 4.1f)), -0.0170F * (getRandomNumber(2.4f, 4.1f)), this.rand.nextGaussian() * 0.0075F).scale(velocity);
		    this.setMotion(vector3d);
		    float f = MathHelper.sqrt(horizontalMag(vector3d));
		    this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		    this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		    this.prevRotationYaw = this.rotationYaw;
		    this.prevRotationPitch = this.rotationPitch;
		}
		
		private BlockState inBlockState;
		
		private boolean func_234593_u_() {
		      return this.inGround && this.world.hasNoCollisions((new AxisAlignedBB(this.getPositionVec(), this.getPositionVec())).grow(0.06D));
		}
		private void func_234594_z_() {
		      this.inGround = false;
		      Vector3d vector3d = this.getMotion();
		      this.setMotion(vector3d.mul(this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F));
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void tick() {
		      super.tick();
		      boolean flag = this.getNoClip();
		      Vector3d vector3d = this.getMotion();
		      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
		         float f = MathHelper.sqrt(horizontalMag(vector3d));
		         this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		         this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		         this.prevRotationYaw = this.rotationYaw;
		         this.prevRotationPitch = this.rotationPitch;
		      }

		      BlockPos blockpos = this.getPosition();
		      BlockState blockstate = this.world.getBlockState(blockpos);
		      if (!blockstate.isAir(this.world, blockpos) && !flag) {
		         VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
		         if (!voxelshape.isEmpty()) {
		            Vector3d vector3d1 = this.getPositionVec();

		            for(AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList()) {
		               if (axisalignedbb.offset(blockpos).contains(vector3d1)) {
		                  this.inGround = true;
		                  break;
		               }
		            }
		         }
		      }

		      if (this.arrowShake > 0) {
		         --this.arrowShake;
		      }

		      if (this.isWet()) {
		         this.extinguish();
		      }

		      if (this.inGround && !flag) {
		         if (this.inBlockState != blockstate && this.func_234593_u_()) {
		            this.func_234594_z_();
		         } else if (!this.world.isRemote) {
		            this.func_225516_i_();
		         }

		         ++this.timeInGround;
		      } else {
		         this.timeInGround = 0;
		         Vector3d vector3d2 = this.getPositionVec();
		         Vector3d vector3d3 = vector3d2.add(vector3d);
		         RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vector3d2, vector3d3, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
		         if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
		            vector3d3 = raytraceresult.getHitVec();
		         }

		         while(!this.removed) {
		            EntityRayTraceResult entityraytraceresult = this.rayTraceEntities(vector3d2, vector3d3);
		            if (entityraytraceresult != null) {
		               raytraceresult = entityraytraceresult;
		            }

		            if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
		               Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
		               Entity entity1 = this.func_234616_v_();
		               if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canAttackPlayer((PlayerEntity)entity)) {
		                  raytraceresult = null;
		                  entityraytraceresult = null;
		               }
		            }

		            if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
		               this.onImpact(raytraceresult);
		               this.isAirBorne = true;
		            }

		            if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
		               break;
		            }

		            raytraceresult = null;
		         }

		         vector3d = this.getMotion();
		         double d3 = vector3d.x;
		         double d4 = vector3d.y;
		         double d0 = vector3d.z;
		         if (this.getIsCritical()) {
		            for(int i = 0; i < 4; ++i) {
		               this.world.addParticle(ParticleTypes.CRIT, this.getPosX() + d3 * i / 4.0D, this.getPosY() + d4 * i / 4.0D, this.getPosZ() + d0 * i / 4.0D, -d3, -d4 + 0.2D, -d0);
		            }
		         }

		         double d5 = this.getPosX() + d3;
		         double d1 = this.getPosY() + d4;
		         double d2 = this.getPosZ() + d0;
		         float f1 = MathHelper.sqrt(horizontalMag(vector3d));
		         if (flag) {
		            this.rotationYaw = (float)(MathHelper.atan2(-d3, -d0) * (180F / (float)Math.PI));
		         } else {
		            this.rotationYaw = (float)(MathHelper.atan2(d3, d0) * (180F / (float)Math.PI));
		         }

		         this.rotationPitch = (float)(MathHelper.atan2(d4, f1) * (180F / (float)Math.PI));
		         this.rotationPitch = func_234614_e_(this.prevRotationPitch, this.rotationPitch);
		         this.rotationYaw = func_234614_e_(this.prevRotationYaw, this.rotationYaw);
		         float f2 = 0.99F;
		         if (this.isInWater()) {
		            for(int j = 0; j < 4; ++j) {
		               this.world.addParticle(ParticleTypes.BUBBLE, d5 - d3 * 0.25D, d1 - d4 * 0.25D, d2 - d0 * 0.25D, d3, d4, d0);
		            }

		            f2 = this.getWaterDrag();
		         }

		         this.setMotion(vector3d.scale(f2));
		         if (!this.hasNoGravity() && !flag) {
		            Vector3d vector3d4 = this.getMotion();
		            this.setMotion(vector3d4.x, vector3d4.y + 0.050d, vector3d4.z);
		         }

		         this.setPosition(d5, d1, d2);
		         this.doBlockCollisions();
		      }
		}
	}

	public static class IronBulletEntity extends AbstractArrowEntity {
		private final Item referenceItem;

	    @SuppressWarnings("unchecked")
	    public IronBulletEntity(EntityType<?> type, World world) {
	        super((EntityType<? extends AbstractArrowEntity>) type, world);
	        this.referenceItem = DeferredRegistryHandler.IRON_MUSKET_BALL.get();
	    }
	    
	    public IronBulletEntity(LivingEntity shooter, World world, Item referenceItemIn) {
	        super(DeferredRegistryHandler.IRON_BULLET_ENTITY.get(), shooter, world);
	        this.referenceItem = referenceItemIn;
	    }

	    public IronBulletEntity(World worldIn, double x, double y, double z) {
	    	super(DeferredRegistryHandler.IRON_BULLET_ENTITY.get(), x, y, z, worldIn);
			this.referenceItem = DeferredRegistryHandler.IRON_MUSKET_BALL.get();
	     }

		@Override
	    public ItemStack getArrowStack() {
	        return new ItemStack(this.referenceItem);
	    }
		
		@Override
		public IPacket<?> createSpawnPacket() {
			return NetworkHooks.getEntitySpawningPacket(this);
		}
		
		@Override
		public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
			Vector3d vector3d = (new Vector3d(x, y, z)).normalize().add(this.rand.nextGaussian() * 0.0025F * (getRandomNumber(0.2f, 1.1f)), 0.00025F * (getRandomNumber(0.2f, 1.1f)), this.rand.nextGaussian() * 0.0025F).scale(velocity);
		    this.setMotion(vector3d);
		    float f = MathHelper.sqrt(horizontalMag(vector3d));
		    this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		    this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		    this.prevRotationYaw = this.rotationYaw;
		    this.prevRotationPitch = this.rotationPitch;
		}
		
		private BlockState inBlockState;
		
		private boolean func_234593_u_() {
		      return this.inGround && this.world.hasNoCollisions((new AxisAlignedBB(this.getPositionVec(), this.getPositionVec())).grow(0.06D));
		}
		private void func_234594_z_() {
		      this.inGround = false;
		      Vector3d vector3d = this.getMotion();
		      this.setMotion(vector3d.mul(this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F));
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void tick() {
		      super.tick();
		      boolean flag = this.getNoClip();
		      Vector3d vector3d = this.getMotion();
		      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
		         float f = MathHelper.sqrt(horizontalMag(vector3d));
		         this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		         this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		         this.prevRotationYaw = this.rotationYaw;
		         this.prevRotationPitch = this.rotationPitch;
		      }

		      BlockPos blockpos = this.getPosition();
		      BlockState blockstate = this.world.getBlockState(blockpos);
		      if (!blockstate.isAir(this.world, blockpos) && !flag) {
		         VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
		         if (!voxelshape.isEmpty()) {
		            Vector3d vector3d1 = this.getPositionVec();

		            for(AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList()) {
		               if (axisalignedbb.offset(blockpos).contains(vector3d1)) {
		                  this.inGround = true;
		                  break;
		               }
		            }
		         }
		      }

		      if (this.arrowShake > 0) {
		         --this.arrowShake;
		      }

		      if (this.isWet()) {
		         this.extinguish();
		      }

		      if (this.inGround && !flag) {
		         if (this.inBlockState != blockstate && this.func_234593_u_()) {
		            this.func_234594_z_();
		         } else if (!this.world.isRemote) {
		            this.func_225516_i_();
		         }

		         ++this.timeInGround;
		      } else {
		         this.timeInGround = 0;
		         Vector3d vector3d2 = this.getPositionVec();
		         Vector3d vector3d3 = vector3d2.add(vector3d);
		         RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vector3d2, vector3d3, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
		         if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
		            vector3d3 = raytraceresult.getHitVec();
		         }

		         while(!this.removed) {
		            EntityRayTraceResult entityraytraceresult = this.rayTraceEntities(vector3d2, vector3d3);
		            if (entityraytraceresult != null) {
		               raytraceresult = entityraytraceresult;
		            }

		            if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
		               Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
		               Entity entity1 = this.func_234616_v_();
		               if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canAttackPlayer((PlayerEntity)entity)) {
		                  raytraceresult = null;
		                  entityraytraceresult = null;
		               }
		            }

		            if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
		               this.onImpact(raytraceresult);
		               this.isAirBorne = true;
		            }

		            if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
		               break;
		            }

		            raytraceresult = null;
		         }

		         vector3d = this.getMotion();
		         double d3 = vector3d.x;
		         double d4 = vector3d.y;
		         double d0 = vector3d.z;
		         if (this.getIsCritical()) {
		            for(int i = 0; i < 4; ++i) {
		               this.world.addParticle(ParticleTypes.CRIT, this.getPosX() + d3 * i / 4.0D, this.getPosY() + d4 * i / 4.0D, this.getPosZ() + d0 * i / 4.0D, -d3, -d4 + 0.2D, -d0);
		            }
		         }

		         double d5 = this.getPosX() + d3;
		         double d1 = this.getPosY() + d4;
		         double d2 = this.getPosZ() + d0;
		         float f1 = MathHelper.sqrt(horizontalMag(vector3d));
		         if (flag) {
		            this.rotationYaw = (float)(MathHelper.atan2(-d3, -d0) * (180F / (float)Math.PI));
		         } else {
		            this.rotationYaw = (float)(MathHelper.atan2(d3, d0) * (180F / (float)Math.PI));
		         }

		         this.rotationPitch = (float)(MathHelper.atan2(d4, f1) * (180F / (float)Math.PI));
		         this.rotationPitch = func_234614_e_(this.prevRotationPitch, this.rotationPitch);
		         this.rotationYaw = func_234614_e_(this.prevRotationYaw, this.rotationYaw);
		         float f2 = 0.99F;
		         if (this.isInWater()) {
		            for(int j = 0; j < 4; ++j) {
		               this.world.addParticle(ParticleTypes.BUBBLE, d5 - d3 * 0.25D, d1 - d4 * 0.25D, d2 - d0 * 0.25D, d3, d4, d0);
		            }

		            f2 = this.getWaterDrag();
		         }

		         this.setMotion(vector3d.scale(f2));
		         if (!this.hasNoGravity() && !flag) {
		            Vector3d vector3d4 = this.getMotion();
		            this.setMotion(vector3d4.x, vector3d4.y + 0.0355d, vector3d4.z);
		         }

		         this.setPosition(d5, d1, d2);
		         this.doBlockCollisions();
		      }
		}
	}

	public static class GoldBulletEntity extends AbstractArrowEntity {
		private final Item referenceItem;

	    @SuppressWarnings("unchecked")
	    public GoldBulletEntity(EntityType<?> type, World world) {
	        super((EntityType<? extends AbstractArrowEntity>) type, world);
	        this.referenceItem = DeferredRegistryHandler.GOLD_MUSKET_BALL.get();
	    }
	    
	    public GoldBulletEntity(LivingEntity shooter, World world, Item referenceItemIn) {
	        super(DeferredRegistryHandler.GOLD_BULLET_ENTITY.get(), shooter, world);
	        this.referenceItem = referenceItemIn;
	    }

	    public GoldBulletEntity(World worldIn, double x, double y, double z) {
	    	super(DeferredRegistryHandler.GOLD_BULLET_ENTITY.get(), x, y, z, worldIn);
			this.referenceItem = DeferredRegistryHandler.GOLD_MUSKET_BALL.get();
	     }

		@Override
	    public ItemStack getArrowStack() {
	        return new ItemStack(this.referenceItem);
	    }
		
		@Override
		public IPacket<?> createSpawnPacket() {
			return NetworkHooks.getEntitySpawningPacket(this);
		}
		
		@Override
		public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
			Vector3d vector3d = (new Vector3d(x, y, z)).normalize().add(this.rand.nextGaussian() * 0.0025F * (getRandomNumber(0.2f, 1.1f)), 0.00025F * (getRandomNumber(0.2f, 1.1f)), this.rand.nextGaussian() * 0.0025F).scale(velocity);
		    this.setMotion(vector3d);
		    float f = MathHelper.sqrt(horizontalMag(vector3d));
		    this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		    this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		    this.prevRotationYaw = this.rotationYaw;
		    this.prevRotationPitch = this.rotationPitch;
		}
		
		private BlockState inBlockState;
		
		private boolean func_234593_u_() {
		      return this.inGround && this.world.hasNoCollisions((new AxisAlignedBB(this.getPositionVec(), this.getPositionVec())).grow(0.06D));
		}
		private void func_234594_z_() {
		      this.inGround = false;
		      Vector3d vector3d = this.getMotion();
		      this.setMotion(vector3d.mul(this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F));
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void tick() {
		      super.tick();
		      boolean flag = this.getNoClip();
		      Vector3d vector3d = this.getMotion();
		      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
		         float f = MathHelper.sqrt(horizontalMag(vector3d));
		         this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		         this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		         this.prevRotationYaw = this.rotationYaw;
		         this.prevRotationPitch = this.rotationPitch;
		      }

		      BlockPos blockpos = this.getPosition();
		      BlockState blockstate = this.world.getBlockState(blockpos);
		      if (!blockstate.isAir(this.world, blockpos) && !flag) {
		         VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
		         if (!voxelshape.isEmpty()) {
		            Vector3d vector3d1 = this.getPositionVec();

		            for(AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList()) {
		               if (axisalignedbb.offset(blockpos).contains(vector3d1)) {
		                  this.inGround = true;
		                  break;
		               }
		            }
		         }
		      }

		      if (this.arrowShake > 0) {
		         --this.arrowShake;
		      }

		      if (this.isWet()) {
		         this.extinguish();
		      }

		      if (this.inGround && !flag) {
		         if (this.inBlockState != blockstate && this.func_234593_u_()) {
		            this.func_234594_z_();
		         } else if (!this.world.isRemote) {
		            this.func_225516_i_();
		         }

		         ++this.timeInGround;
		      } else {
		         this.timeInGround = 0;
		         Vector3d vector3d2 = this.getPositionVec();
		         Vector3d vector3d3 = vector3d2.add(vector3d);
		         RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vector3d2, vector3d3, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
		         if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
		            vector3d3 = raytraceresult.getHitVec();
		         }

		         while(!this.removed) {
		            EntityRayTraceResult entityraytraceresult = this.rayTraceEntities(vector3d2, vector3d3);
		            if (entityraytraceresult != null) {
		               raytraceresult = entityraytraceresult;
		            }

		            if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
		               Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
		               Entity entity1 = this.func_234616_v_();
		               if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canAttackPlayer((PlayerEntity)entity)) {
		                  raytraceresult = null;
		                  entityraytraceresult = null;
		               }
		            }

		            if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
		               this.onImpact(raytraceresult);
		               this.isAirBorne = true;
		            }

		            if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
		               break;
		            }

		            raytraceresult = null;
		         }

		         vector3d = this.getMotion();
		         double d3 = vector3d.x;
		         double d4 = vector3d.y;
		         double d0 = vector3d.z;
		         if (this.getIsCritical()) {
		            for(int i = 0; i < 4; ++i) {
		               this.world.addParticle(ParticleTypes.CRIT, this.getPosX() + d3 * i / 4.0D, this.getPosY() + d4 * i / 4.0D, this.getPosZ() + d0 * i / 4.0D, -d3, -d4 + 0.2D, -d0);
		            }
		         }

		         double d5 = this.getPosX() + d3;
		         double d1 = this.getPosY() + d4;
		         double d2 = this.getPosZ() + d0;
		         float f1 = MathHelper.sqrt(horizontalMag(vector3d));
		         if (flag) {
		            this.rotationYaw = (float)(MathHelper.atan2(-d3, -d0) * (180F / (float)Math.PI));
		         } else {
		            this.rotationYaw = (float)(MathHelper.atan2(d3, d0) * (180F / (float)Math.PI));
		         }

		         this.rotationPitch = (float)(MathHelper.atan2(d4, f1) * (180F / (float)Math.PI));
		         this.rotationPitch = func_234614_e_(this.prevRotationPitch, this.rotationPitch);
		         this.rotationYaw = func_234614_e_(this.prevRotationYaw, this.rotationYaw);
		         float f2 = 0.99F;
		         if (this.isInWater()) {
		            for(int j = 0; j < 4; ++j) {
		               this.world.addParticle(ParticleTypes.BUBBLE, d5 - d3 * 0.25D, d1 - d4 * 0.25D, d2 - d0 * 0.25D, d3, d4, d0);
		            }

		            f2 = this.getWaterDrag();
		         }

		         this.setMotion(vector3d.scale(f2));
		         if (!this.hasNoGravity() && !flag) {
		            Vector3d vector3d4 = this.getMotion();
		            this.setMotion(vector3d4.x, vector3d4.y + 0.0355d, vector3d4.z);
		         }

		         this.setPosition(d5, d1, d2);
		         this.doBlockCollisions();
		      }
		}
	}

	public static class DiamondBulletEntity extends AbstractArrowEntity {
		private final Item referenceItem;

	    @SuppressWarnings("unchecked")
	    public DiamondBulletEntity(EntityType<?> type, World world) {
	        super((EntityType<? extends AbstractArrowEntity>) type, world);
	        this.referenceItem = DeferredRegistryHandler.DIAMOND_MUSKET_BALL.get();
	    }
	    
	    public DiamondBulletEntity(LivingEntity shooter, World world, Item referenceItemIn) {
	        super(DeferredRegistryHandler.DIAMOND_BULLET_ENTITY.get(), shooter, world);
	        this.referenceItem = referenceItemIn;
	    }

	    public DiamondBulletEntity(World worldIn, double x, double y, double z) {
	    	super(DeferredRegistryHandler.DIAMOND_BULLET_ENTITY.get(), x, y, z, worldIn);
			this.referenceItem = DeferredRegistryHandler.DIAMOND_MUSKET_BALL.get();
	     }

		@Override
	    public ItemStack getArrowStack() {
	        return new ItemStack(this.referenceItem);
	    }
		
		@Override
		public IPacket<?> createSpawnPacket() {
			return NetworkHooks.getEntitySpawningPacket(this);
		}
		
		@Override
		public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
			Vector3d vector3d = (new Vector3d(x, y, z)).normalize().add(this.rand.nextGaussian() * 0.0025F * (getRandomNumber(0.2f, 0.9f)), 0.00025F * (getRandomNumber(0.2f, 0.9f)), this.rand.nextGaussian() * 0.0025F).scale(velocity);
		    this.setMotion(vector3d);
		    float f = MathHelper.sqrt(horizontalMag(vector3d));
		    this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		    this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		    this.prevRotationYaw = this.rotationYaw;
		    this.prevRotationPitch = this.rotationPitch;
		}
		
		private BlockState inBlockState;
		
		private boolean func_234593_u_() {
		      return this.inGround && this.world.hasNoCollisions((new AxisAlignedBB(this.getPositionVec(), this.getPositionVec())).grow(0.06D));
		}
		private void func_234594_z_() {
		      this.inGround = false;
		      Vector3d vector3d = this.getMotion();
		      this.setMotion(vector3d.mul(this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F));
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void tick() {
		      super.tick();
		      boolean flag = this.getNoClip();
		      Vector3d vector3d = this.getMotion();
		      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
		         float f = MathHelper.sqrt(horizontalMag(vector3d));
		         this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		         this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		         this.prevRotationYaw = this.rotationYaw;
		         this.prevRotationPitch = this.rotationPitch;
		      }

		      BlockPos blockpos = this.getPosition();
		      BlockState blockstate = this.world.getBlockState(blockpos);
		      if (!blockstate.isAir(this.world, blockpos) && !flag) {
		         VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
		         if (!voxelshape.isEmpty()) {
		            Vector3d vector3d1 = this.getPositionVec();

		            for(AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList()) {
		               if (axisalignedbb.offset(blockpos).contains(vector3d1)) {
		                  this.inGround = true;
		                  break;
		               }
		            }
		         }
		      }

		      if (this.arrowShake > 0) {
		         --this.arrowShake;
		      }

		      if (this.isWet()) {
		         this.extinguish();
		      }

		      if (this.inGround && !flag) {
		         if (this.inBlockState != blockstate && this.func_234593_u_()) {
		            this.func_234594_z_();
		         } else if (!this.world.isRemote) {
		            this.func_225516_i_();
		         }

		         ++this.timeInGround;
		      } else {
		         this.timeInGround = 0;
		         Vector3d vector3d2 = this.getPositionVec();
		         Vector3d vector3d3 = vector3d2.add(vector3d);
		         RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vector3d2, vector3d3, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
		         if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
		            vector3d3 = raytraceresult.getHitVec();
		         }

		         while(!this.removed) {
		            EntityRayTraceResult entityraytraceresult = this.rayTraceEntities(vector3d2, vector3d3);
		            if (entityraytraceresult != null) {
		               raytraceresult = entityraytraceresult;
		            }

		            if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
		               Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
		               Entity entity1 = this.func_234616_v_();
		               if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canAttackPlayer((PlayerEntity)entity)) {
		                  raytraceresult = null;
		                  entityraytraceresult = null;
		               }
		            }

		            if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
		               this.onImpact(raytraceresult);
		               this.isAirBorne = true;
		            }

		            if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
		               break;
		            }

		            raytraceresult = null;
		         }

		         vector3d = this.getMotion();
		         double d3 = vector3d.x;
		         double d4 = vector3d.y;
		         double d0 = vector3d.z;
		         if (this.getIsCritical()) {
		            for(int i = 0; i < 4; ++i) {
		               this.world.addParticle(ParticleTypes.CRIT, this.getPosX() + d3 * i / 4.0D, this.getPosY() + d4 * i / 4.0D, this.getPosZ() + d0 * i / 4.0D, -d3, -d4 + 0.2D, -d0);
		            }
		         }

		         double d5 = this.getPosX() + d3;
		         double d1 = this.getPosY() + d4;
		         double d2 = this.getPosZ() + d0;
		         float f1 = MathHelper.sqrt(horizontalMag(vector3d));
		         if (flag) {
		            this.rotationYaw = (float)(MathHelper.atan2(-d3, -d0) * (180F / (float)Math.PI));
		         } else {
		            this.rotationYaw = (float)(MathHelper.atan2(d3, d0) * (180F / (float)Math.PI));
		         }

		         this.rotationPitch = (float)(MathHelper.atan2(d4, f1) * (180F / (float)Math.PI));
		         this.rotationPitch = func_234614_e_(this.prevRotationPitch, this.rotationPitch);
		         this.rotationYaw = func_234614_e_(this.prevRotationYaw, this.rotationYaw);
		         float f2 = 0.99F;
		         if (this.isInWater()) {
		            for(int j = 0; j < 4; ++j) {
		               this.world.addParticle(ParticleTypes.BUBBLE, d5 - d3 * 0.25D, d1 - d4 * 0.25D, d2 - d0 * 0.25D, d3, d4, d0);
		            }

		            f2 = this.getWaterDrag();
		         }

		         this.setMotion(vector3d.scale(f2));
		         if (!this.hasNoGravity() && !flag) {
		            Vector3d vector3d4 = this.getMotion();
		            this.setMotion(vector3d4.x, vector3d4.y + 0.0385d, vector3d4.z);
		         }

		         this.setPosition(d5, d1, d2);
		         this.doBlockCollisions();
		      }
		}
	}

	public static class NetheriteBulletEntity extends AbstractArrowEntity {
		private final Item referenceItem;

	    @SuppressWarnings("unchecked")
	    public NetheriteBulletEntity(EntityType<?> type, World world) {
	        super((EntityType<? extends AbstractArrowEntity>) type, world);
	        this.referenceItem = DeferredRegistryHandler.NETHERITE_MUSKET_BALL.get();
	    }
	    
	    public NetheriteBulletEntity(LivingEntity shooter, World world, Item referenceItemIn) {
	        super(DeferredRegistryHandler.NETHERITE_BULLET_ENTITY.get(), shooter, world);
	        this.referenceItem = referenceItemIn;
	    }

	    public NetheriteBulletEntity(World worldIn, double x, double y, double z) {
	    	super(DeferredRegistryHandler.NETHERITE_BULLET_ENTITY.get(), x, y, z, worldIn);
			this.referenceItem = DeferredRegistryHandler.NETHERITE_MUSKET_BALL.get();
	     }

		@Override
	    public ItemStack getArrowStack() {
	        return new ItemStack(this.referenceItem);
	    }
		
		@Override
		public IPacket<?> createSpawnPacket() {
			return NetworkHooks.getEntitySpawningPacket(this);
		}
		
		@Override
		public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
			Vector3d vector3d = (new Vector3d(x, y, z)).normalize().add(this.rand.nextGaussian() * 0.0020F * (getRandomNumber(0.2f, 0.7f)), 0.0020F * (getRandomNumber(0.2f, 0.7f)), this.rand.nextGaussian() * 0.0020F).scale(velocity);
		    this.setMotion(vector3d);
		    float f = MathHelper.sqrt(horizontalMag(vector3d));
		    this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		    this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		    this.prevRotationYaw = this.rotationYaw;
		    this.prevRotationPitch = this.rotationPitch;
		}
		
		private BlockState inBlockState;
		
		private boolean func_234593_u_() {
		      return this.inGround && this.world.hasNoCollisions((new AxisAlignedBB(this.getPositionVec(), this.getPositionVec())).grow(0.06D));
		}
		private void func_234594_z_() {
		      this.inGround = false;
		      Vector3d vector3d = this.getMotion();
		      this.setMotion(vector3d.mul(this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F, this.rand.nextFloat() * 0.4F));
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void tick() {
		      super.tick();
		      boolean flag = this.getNoClip();
		      Vector3d vector3d = this.getMotion();
		      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
		         float f = MathHelper.sqrt(horizontalMag(vector3d));
		         this.rotationYaw = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float)Math.PI));
		         this.rotationPitch = (float)(MathHelper.atan2(vector3d.y, f) * (180F / (float)Math.PI));
		         this.prevRotationYaw = this.rotationYaw;
		         this.prevRotationPitch = this.rotationPitch;
		      }

		      BlockPos blockpos = this.getPosition();
		      BlockState blockstate = this.world.getBlockState(blockpos);
		      if (!blockstate.isAir(this.world, blockpos) && !flag) {
		         VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
		         if (!voxelshape.isEmpty()) {
		            Vector3d vector3d1 = this.getPositionVec();

		            for(AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList()) {
		               if (axisalignedbb.offset(blockpos).contains(vector3d1)) {
		                  this.inGround = true;
		                  break;
		               }
		            }
		         }
		      }

		      if (this.arrowShake > 0) {
		         --this.arrowShake;
		      }

		      if (this.isWet()) {
		         this.extinguish();
		      }

		      if (this.inGround && !flag) {
		         if (this.inBlockState != blockstate && this.func_234593_u_()) {
		            this.func_234594_z_();
		         } else if (!this.world.isRemote) {
		            this.func_225516_i_();
		         }

		         ++this.timeInGround;
		      } else {
		         this.timeInGround = 0;
		         Vector3d vector3d2 = this.getPositionVec();
		         Vector3d vector3d3 = vector3d2.add(vector3d);
		         RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vector3d2, vector3d3, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
		         if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
		            vector3d3 = raytraceresult.getHitVec();
		         }

		         while(!this.removed) {
		            EntityRayTraceResult entityraytraceresult = this.rayTraceEntities(vector3d2, vector3d3);
		            if (entityraytraceresult != null) {
		               raytraceresult = entityraytraceresult;
		            }

		            if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
		               Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
		               Entity entity1 = this.func_234616_v_();
		               if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canAttackPlayer((PlayerEntity)entity)) {
		                  raytraceresult = null;
		                  entityraytraceresult = null;
		               }
		            }

		            if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
		               this.onImpact(raytraceresult);
		               this.isAirBorne = true;
		            }

		            if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
		               break;
		            }

		            raytraceresult = null;
		         }

		         vector3d = this.getMotion();
		         double d3 = vector3d.x;
		         double d4 = vector3d.y;
		         double d0 = vector3d.z;
		         if (this.getIsCritical()) {
		            for(int i = 0; i < 4; ++i) {
		               this.world.addParticle(ParticleTypes.CRIT, this.getPosX() + d3 * i / 4.0D, this.getPosY() + d4 * i / 4.0D, this.getPosZ() + d0 * i / 4.0D, -d3, -d4 + 0.2D, -d0);
		            }
		         }

		         double d5 = this.getPosX() + d3;
		         double d1 = this.getPosY() + d4;
		         double d2 = this.getPosZ() + d0;
		         float f1 = MathHelper.sqrt(horizontalMag(vector3d));
		         if (flag) {
		            this.rotationYaw = (float)(MathHelper.atan2(-d3, -d0) * (180F / (float)Math.PI));
		         } else {
		            this.rotationYaw = (float)(MathHelper.atan2(d3, d0) * (180F / (float)Math.PI));
		         }

		         this.rotationPitch = (float)(MathHelper.atan2(d4, f1) * (180F / (float)Math.PI));
		         this.rotationPitch = func_234614_e_(this.prevRotationPitch, this.rotationPitch);
		         this.rotationYaw = func_234614_e_(this.prevRotationYaw, this.rotationYaw);
		         float f2 = 0.99F;
		         if (this.isInWater()) {
		            for(int j = 0; j < 4; ++j) {
		               this.world.addParticle(ParticleTypes.BUBBLE, d5 - d3 * 0.25D, d1 - d4 * 0.25D, d2 - d0 * 0.25D, d3, d4, d0);
		            }

		            f2 = this.getWaterDrag();
		         }

		         this.setMotion(vector3d.scale(f2));
		         if (!this.hasNoGravity() && !flag) {
		            Vector3d vector3d4 = this.getMotion();
		            this.setMotion(vector3d4.x, vector3d4.y + 0.04d, vector3d4.z);
		         }

		         this.setPosition(d5, d1, d2);
		         this.doBlockCollisions();
		      }
		}
	}
}