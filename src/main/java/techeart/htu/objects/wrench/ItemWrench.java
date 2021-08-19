package techeart.htu.objects.wrench;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import techeart.htu.CreativeTabs;
import techeart.htu.utils.WorldUtils;
import techeart.htu.utils.registration.HTUItem;

public class ItemWrench extends HTUItem implements IWrench
{
    private final Multimap<Attribute, AttributeModifier> toolAttributes;

    public ItemWrench(int maxUsages, float attackDamage, float attackSpeed) { this(maxUsages, attackDamage, attackSpeed, CreativeTabs.STEAM_AGE); }

    public ItemWrench(int maxUsages, float attackDamage, float attackSpeed, ItemGroup creativeTab)
    {
        super(new Properties()
                .maxStackSize(1)
                .defaultMaxDamage(maxUsages)
                .group(creativeTab)
        );

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        this.toolAttributes = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot)
    {
        return equipmentSlot == EquipmentSlotType.MAINHAND ? this.toolAttributes : super.getAttributeModifiers(equipmentSlot);
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker)
    {
        stack.damageItem(1, attacker, (entity) -> {
            entity.sendBreakAnimation(EquipmentSlotType.MAINHAND);
        });
        return true;
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context)
    {
        TileEntity tile = WorldUtils.getTileEntity(context.getWorld(), context.getPos());
        if(tile instanceof IWrenchTarget)
        {
            PlayerEntity player = context.getPlayer();
            if(player == null) return ActionResultType.PASS;

            if(((IWrenchTarget) tile).onWrenchUsed(player.isSneaking()))
                return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
}
