# Tutorial de Teste de Integração

**c) 2023 [Cleuton Sampaio](https://linkedin.com/in/cleutonsampaio)**

Testes de integração devem validar o **backend** do software, verificando se as interfaces e integrações entre os componetes estão funcionando de acordo com a especificação. 

Há uma certa confusão entre testes **unitários** e de **integração**, mas os objetivos são diferentes:
- **Teste unitário**: Validar a lógica de cada unidade (método, função) do software.
- **Teste de integração**: Validar e integração entre as várias unidades e componentes internos e externos ao software.

Leia o [**arquivo PDF**](./testes-integracao.pdf) sobre testes de integração antes de prosseguir.

## O que deve ser testado ##

Um **caso de uso** completo, do ponto de vista do **backend**. Neste projeto, testaremos o caso de uso de postar uma mensagem: 

![](./caso-de-uso.png)

Vou pular a parte de **login** para descomplicar as coisas, mas, a rigor, deveríamos testar a partir do login mesmo. Vamos precisar executar os componentes do software sem **mockar** nada, nem mesmo o banco de dados. 
O problema é: 

- *Como testar com banco de dados sem deixar efeitos colaterais?*

Sim, a cada teste, podemos deixar o estado do sistema de um jeito inconsistente. A não ser que queiramos "encadear" testes, o que não é uma boa ideia. Para facilitar as coisas, utilizaremos dois componentes bem interessantes:
- **Testcontainers**: Uma biblioteca simples e prática para utilizarmos **contêineres** **Docker** em nossos testes.
- **FailSafe**: Um plugin do **maven** para executar testes de integração. 

## Dependências ##

Para começar, vamos acrescentar as dependências no **pom.xml**:

```xml
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>1.19.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>1.19.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>mysql</artifactId>
            <version>1.19.0</version>
            <scope>test</scope>
        </dependency>
```

São duas dependências para o **Testcontainers** e sua integração com o **Junit Jupiter** e uma para o módulo **mysql**.

E temos que configurar os **plugins** na seção **build**: 

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.1.2</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

## Escrevendo um teste de integração ##

O **Failsafe** é configurado para executar os testes que terminam em "*IT". Você pode até alterar a configuração para mudar a pasta onde eles ficam, mas, se quiser, pode deixar em **src/test/java** mesmo.

Os testes de integração são executados em dois **goals** do ciclo de vida do **maven**: 
- **integration-test**: Só executa os testes de integração.
- **verify**: Executa todos os testes. 

O que vamos testar? Bom, vamos postar uma mensagem! Para começar, examine o código de teste de integração [**BaseIT.java**](./src/test/java/com/pythondrops/testing/BaseIT.java). Começamos criando um **contêiner** para o **mysql**: 

```
    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        mysql = new MySQLContainer<>("mysql")
          .withUsername("root")
          .withPassword("my-secret-pw")
          .withExposedPorts(3306)
          .withCopyFileToContainer(MountableFile.forClasspathResource("database.sql"), "/docker-entrypoint-initdb.d/schema.sql");

        mysql.start();

        MysqlDataSource mds = new MysqlDataSource();
        mds.setUser("root");
        mds.setPassword("my-secret-pw");
        mds.setServerName("localhost");
        mds.setPort(mysql.getFirstMappedPort().intValue());
        mds.setDatabaseName("TESTDB");

        dataSource = mds;
    }
```

Este código será executado antes de qualquer teste. Se você quiser reaproveitar, pode utilizar a anotação **@BeforeAll**, mas lembre-se que os efeitos colaterais de um teste poderão afetar outros testes.

Estou criando uma instância **Docker** do **mysql** e iniciando o contêiner. Note que passei a opção **withCopyFileToContainer** que copiará o arquivo **SQL** de criação de esquema e inserção de dados, o que deixará o database pronto para uso. 

Agora vem o código do teste: 

```
    @Test
    public void itPostMessageOK() throws SQLException, UserNotAllowedException, ChannelNotAvailableException, ParseException {
        System.out.println("IT post a message with no errors");

        // Given:

        DatabaseWrapper dbWrapper = new DatabaseWrapper(dataSource);
        DemoCode dc = new DemoCode(dbWrapper);

        // When:
        Date postDate = new Date();
        UUID messageId = dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), "TITLE", "Message content");

        // Then:

        Connection db = this.dataSource.getConnection();
        PreparedStatement query = db.prepareStatement("SELECT BIN_TO_UUID(ID) AS MESSAGE_ID, BIN_TO_UUID(AUTHOR) AS MESSAGE_AUTHOR, "
          + "BIN_TO_UUID(CHANNEL_ID) AS CHANNEL_ID, TITLE, CONTENT, CREATED_TIME FROM MESSAGE;");
        ResultSet rs = query.executeQuery();
        int count = 0;
        while (rs.next()) {
            count++;
            if (count > 1) {
                fail("There should be only one message");
            }
            String dbMessageId = rs.getString("MESSAGE_ID");
            String dbAuthorId = rs.getString("MESSAGE_AUTHOR");
            String dbChannelId = rs.getString("CHANNEL_ID");
            String dbTitle = rs.getString("TITLE");
            String dbContent = rs.getString("CONTENT");
            String dbDate = rs.getString("CREATED_TIME");
            System.out.println(dbDate);

            SimpleDateFormat inputSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            inputSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date myDate = inputSDF.parse(dbDate);
            SimpleDateFormat outputSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dbConvertedDate = outputSDF.parse(outputSDF.format(myDate));
            System.out.println(dbConvertedDate);

            long timeDiff = abs(postDate.getTime() - dbConvertedDate.getTime());

            assertTrue(timeDiff < 5000);


        }
    }
```

É basicamente o teste do "caminho feliz" dos testes unitários, só que sem **mockar** coisa alguma. O mais importante é validar o estado do software após o teste, o que eu faço dentro do *loop*. Só pode haver um registro gravado na tabela **MESSAGE** e ele tem que ter o conteúdo específico que estou passando.

Uma nota interessante que fará você ver a importância dos **testes de integração**: O teste do formato da data de postagem da mensagem. Eu criei essa coluna de propósito para demonstrar isso. Nos testes unitários nem nos preocupamos em testar, embora pudéssemos. Mas aqui, você tomará logo um "pescotapa" ao constatar as diferenças de **timezone** entre o **mysql** e o **java**.
Quando declaramos uma coluna do tipo **TIMESTAMP** no **mysql** ele sempre gravará e retornará a data/hora na timezone **UTC**, e eu estou testando se a diferença entre a data gravada e a data antes de postar a mensagem é menor que 5 segundos (pode haver algum delay), só para garantir que a data correta foi gravada. Eu usei um "truque" simples para obter a data e convertê-la para o **timezone** padrão.

Este é só um dos tipos de problema que você só pega quando faz testes de integração.

Agora é só rodar: ```mvn verify``` e pronto!

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.pythondrops.testing.DatabaseWrapperTest
Testing user is suspended on a channel
Testing a suspende user
Testing a hidden channel
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.678 s -- in com.pythondrops.testing.DatabaseWrapperTest
[INFO] Running com.pythondrops.testing.DemoCodeTest
Testing title zero length argument
Testing all zero length arguments
MESSAGE: Invalid UUID string: 
Testing content null argument
Testing content zero length argument
Testing all null arguments
Testing non existent user
Testing channelId null argument
Testing title null argument
Testing a complete post message
Testing userId null argument
Testing user not in channel
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s -- in com.pythondrops.testing.DemoCodeTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ TesteProject ---
[INFO] 
[INFO] --- maven-failsafe-plugin:3.1.2:integration-test (default) @ TesteProject ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.pythondrops.testing.BaseIT
IT post a message with no errors
2023-09-11 18:52:45
Mon Sep 11 15:52:45 BRT 2023
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 8.968 s -- in com.pythondrops.testing.BaseIT
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- maven-failsafe-plugin:3.1.2:verify (default) @ TesteProject ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  11.385 s
[INFO] Finished at: 2023-09-11T15:52:45-03:00
[INFO] ------------------------------------------------------------------------

Process finished with exit code 0

```

Os testes unitários e de integração passaram sem problemas.

**Mas quais outros testes eu preciso fazer?**
Neste exemplo, só criei um único teste, mas você pode testar o caso do usuário não estar no canal ou mesmo do canal não existir, embora já tenhamos testado tudo isso nos testes unitários. Eu sugeriria que você testasse coisas mais críticas, como o **mysql** fora do ar, ou chaves duplicadas (o que não é o caso aqui).
