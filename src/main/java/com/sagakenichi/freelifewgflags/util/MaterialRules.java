package com.sagakenichi.freelifewgflags.util;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class MaterialRules {

    private final boolean all;
    private final Set<Material> materials;

    private MaterialRules(boolean all, Set<Material> materials) {
        this.all = all;
        this.materials = Set.copyOf(materials);
    }

    public static MaterialRules parse(String input) {
        if (input == null || input.isBlank()) {
            return new MaterialRules(false, Set.of());
        }
        boolean all = false;
        Set<Material> materials = new HashSet<>();
        for (String raw : input.split("[,;]")) {
            String token = raw.trim();
            if (token.equals("*")) {
                all = true;
                continue;
            }
            if (token.equalsIgnoreCase("none")) {
                continue;
            }
            Material material = Material.matchMaterial(token.toUpperCase(Locale.ROOT));
            if (material != null && material.isBlock()) {
                materials.add(material);
            }
        }
        return new MaterialRules(all, materials);
    }

    public boolean allows(Material material) {
        return all || materials.contains(material);
    }
}
