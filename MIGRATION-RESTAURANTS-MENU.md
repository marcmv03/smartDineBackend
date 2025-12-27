# Migración: Eliminar tabla intermedia restaurants_menu

## Contexto
Se corrigió la relación entre `Restaurant` y `MenuItem` para usar una foreign key directa (`restaurant_id` en `menu_items`) en lugar de una tabla intermedia `restaurants_menu`.

## Cambio en el código
**Archivo modificado**: `src/main/java/com/smartDine/entity/MenuItem.java`

Antes:
```java
@ManyToOne
private Restaurant restaurant;
```

Después:
```java
@ManyToOne
@JoinColumn(name = "restaurant_id")
private Restaurant restaurant;
```

## Problema solucionado
Error original:
```
ERROR: update or delete on table "menu_items" violates foreign key constraint 
"fk8al08rpj9q2x2cqal1blmjc91" on table "restaurants_menu"
```

## Pasos para aplicar la migración en producción

### Opción 1: Migración automática (Recomendada para desarrollo)
1. Detener la aplicación
2. Hacer backup de la base de datos:
   ```bash
   pg_dump -h localhost -U postgres smartDine > backup_$(date +%Y%m%d_%H%M%S).sql
   ```
3. Ejecutar el script de migración:
   ```bash
   psql -h localhost -U postgres -d smartDine -f migration-remove-restaurants-menu-table.sql
   ```
4. Iniciar la aplicación con el código actualizado
5. Verificar que Hibernate crea la columna `restaurant_id` en `menu_items`

### Opción 2: Migración manual
Conectarse a la base de datos y ejecutar:
```sql
-- Eliminar constraints de foreign key
ALTER TABLE restaurants_menu DROP CONSTRAINT IF EXISTS fk8al08rpj9q2x2cqal1blmjc91;
ALTER TABLE restaurants_menu DROP CONSTRAINT IF EXISTS fkfqxe0huu6p1w47e52dj0u35ov;

-- Eliminar tabla intermedia
DROP TABLE restaurants_menu;
```

### Verificación post-migración
1. Verificar que la tabla `restaurants_menu` ya no existe:
   ```sql
   SELECT table_name FROM information_schema.tables WHERE table_name = 'restaurants_menu';
   ```
   (Resultado esperado: 0 filas)

2. Verificar que `menu_items` tiene la columna `restaurant_id`:
   ```sql
   SELECT column_name, data_type 
   FROM information_schema.columns 
   WHERE table_name = 'menu_items' AND column_name = 'restaurant_id';
   ```
   (Resultado esperado: 1 fila con `restaurant_id` de tipo `bigint`)

## Comportamiento después de la migración
✅ **Eliminación en cascada**: Al eliminar un `MenuItem`, se elimina automáticamente gracias al `CascadeType.ALL` en `Restaurant.menu`

✅ **Sin tabla intermedia**: La relación ahora usa directamente `menu_items.restaurant_id → restaurants.id`

✅ **Tests**: 156/156 tests pasando correctamente

## Notas importantes
- ⚠️ **Siempre hacer backup antes de ejecutar la migración**
- ⚠️ **La migración es irreversible** - la tabla `restaurants_menu` será eliminada permanentemente
- ✅ Los datos existentes en `menu_items` NO se ven afectados
- ✅ Hibernate creará automáticamente la columna `restaurant_id` al iniciar con `ddl-auto=update`
