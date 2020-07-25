package com.anonymoushacker1279.immersiveweapons.items;

import com.anonymoushacker1279.immersiveweapons.ImmersiveWeapons;
import com.anonymoushacker1279.immersiveweapons.util.CustomArmorMaterials;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MoltenBoots extends ArmorItem {

	public MoltenBoots() {
		super(CustomArmorMaterials.MOLTEN, EquipmentSlotType.FEET, (new Item.Properties().group(ImmersiveWeapons.TAB)));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
		return ImmersiveWeapons.MOD_ID+":textures/armor/molten_layer_1.png";
	}
}