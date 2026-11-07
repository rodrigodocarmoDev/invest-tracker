package security;

/**
 * Papéis (roles) existentes no sistema.
 *
 * Por que constantes String e não um enum?
 * Anotações como @RolesAllowed só aceitam valores constantes em tempo de compilação.
 * Um enum não pode ser usado diretamente ali, mas uma constante "static final String" pode:
 *   @RolesAllowed(Roles.ADMIN)
 */
public final class Roles {

    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";

    private Roles() {
    }
}
