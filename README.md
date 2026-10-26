# CRUD de funcionarios (Java Swing + patrón DAO + MySQL)

Aplicación de escritorio para el área de Recursos Humanos: permite **listar, crear, editar y eliminar**
funcionarios, junto con su **tipo de documento, estado civil, grupo familiar y formación académica**.

## Estructura (capas)

| Paquete | Responsabilidad |
|---|---|
| `modelo` | Clases que representan los datos (Funcionario, Catalogo, GrupoFamiliar, FormacionAcademica) |
| `conexion` | Abre la conexión a MySQL leyendo `config.properties` |
| `dao` | Patrón DAO: interfaces (`FuncionarioDAO`, `CatalogoDAO`) e implementaciones JDBC |
| `excepciones` | `DaoException` (errores de base de datos) y `ValidacionException` (datos inválidos) |
| `vista` | Interfaz Swing: ventana principal y formulario |
| `principal` | `Main`, punto de entrada |

## Cómo ejecutarlo

1. **Base de datos:** en MySQL Workbench ejecute en orden `sql/01_estructura.sql` y `sql/02_poblado.sql`.
2. **Configuración:** copie `src/main/resources/config.properties.example` como
   `src/main/resources/config.properties` y escriba su contraseña de MySQL.
3. **Proyecto:** abra la carpeta como proyecto Maven (NetBeans, IntelliJ o Eclipse). Maven descargará el
   conector `mysql-connector-j` automáticamente.
4. **Ejecutar:** corra la clase `principal.Main`.

Requisitos: JDK 11 o superior, MySQL 8, Maven (incluido en los IDE).

## Seguridad aplicada

- Consultas con `PreparedStatement` (evitan la inyección SQL).
- La contraseña no está en el código: vive en `config.properties`, excluido de Git.
- Validación de los datos del formulario antes de llegar a la base de datos.
- Transacciones (commit/rollback) al crear y editar, para no dejar datos a medias.
- Mensajes de error claros para el usuario, sin mostrar detalles internos de la base de datos.

## Integrantes

- Jose Daniel Novoa Hernandez
- (agregar los demás integrantes, máximo 4)
