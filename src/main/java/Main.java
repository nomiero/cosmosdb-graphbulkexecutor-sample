import com.google.common.base.Stopwatch;
import com.microsoft.azure.documentdb.*;
import com.microsoft.azure.documentdb.bulkexecutor.BulkDeleteResponse;
import com.microsoft.azure.documentdb.bulkexecutor.BulkImportResponse;
import com.microsoft.azure.documentdb.bulkexecutor.BulkUpdateResponse;
import com.microsoft.azure.documentdb.bulkexecutor.graph.Element.GremlinEdge;
import com.microsoft.azure.documentdb.bulkexecutor.graph.Element.GremlinVertex;
import com.microsoft.azure.documentdb.bulkexecutor.graph.GraphBulkExecutor;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.process.computer.GraphFilter;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.io.GraphReader;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONIo;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONReader;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONVersion;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.apache.tinkerpop.gremlin.structure.util.star.StarGraph;
import sun.invoke.empty.Empty;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Main {

    static final String insertQueries[] = new String[] {
            "g.V().drop()",
            "g.addV(id, '4DKFHKLWMZIC', label, 'product').property('VertexKey', '4DKFHKLWMZIC').as('4DKFHKLWMZIC').addV(id, '4BLKBEVWAPP0', label, 'product').property('VertexKey', '4BLKBEVWAPP0').as('4BLKBEVWAPP0').addV(id, '4EWTQJUPJS1S', label, 'product').property('VertexKey', '4EWTQJUPJS1S').as('4EWTQJUPJS1S').addV(id, '1H0A6GMRBS48', label, 'product').property('VertexKey', '1H0A6GMRBS48').as('1H0A6GMRBS48').addV(id, '5JPAP4IVTZLU', label, 'product').property('VertexKey', '5JPAP4IVTZLU').as('5JPAP4IVTZLU').addV(id, '612GAUDUEKYQ', label, 'product').property('VertexKey', '612GAUDUEKYQ').as('612GAUDUEKYQ').addV(id, '5EVD9JLJCOAB', label, 'product').property('VertexKey', '5EVD9JLJCOAB').as('5EVD9JLJCOAB').addV(id, '1TJWVSFXHYLR', label, 'product').property('VertexKey', '1TJWVSFXHYLR').as('1TJWVSFXHYLR').addV(id, '5J9V2I0NV3Z7', label, 'product').property('VertexKey', '5J9V2I0NV3Z7').as('5J9V2I0NV3Z7').addV(id, '6BWTJ6EQ9J8N', label, 'product').property('VertexKey', '6BWTJ6EQ9J8N').as('6BWTJ6EQ9J8N').addV(id, '6B46RP7GSJIF', label, 'product').property('VertexKey', '6B46RP7GSJIF').as('6B46RP7GSJIF').addV(id, '1LIG3BROLNG4', label, 'product').property('VertexKey', '1LIG3BROLNG4').as('1LIG3BROLNG4').addV(id, '1XF76DBHBZ4W', label, 'product').property('VertexKey', '1XF76DBHBZ4W').as('1XF76DBHBZ4W').addV(id, '4DNVWHXO3GAP', label, 'product').property('VertexKey', '4DNVWHXO3GAP').as('4DNVWHXO3GAP').addV(id, '4S7K07HB8NBA', label, 'product').property('VertexKey', '4S7K07HB8NBA').as('4S7K07HB8NBA').addV(id, '1CYK1R0DLM3I', label, 'product').property('VertexKey', '1CYK1R0DLM3I').as('1CYK1R0DLM3I').addV(id, '1HSVFWCHHBE7', label, 'product').property('VertexKey', '1HSVFWCHHBE7').as('1HSVFWCHHBE7').addV(id, '3WR2HNMZB91X', label, 'product').property('VertexKey', '3WR2HNMZB91X').as('3WR2HNMZB91X').addV(id, '69CCYEIMKL3B', label, 'product').property('VertexKey', '69CCYEIMKL3B').as('69CCYEIMKL3B')",
            "g.addV('product').property('color', 'Other').property('image_url', 'https://i5.walmartimages.com/asr/6a4ebcea-c8ff-4ba1-9862-8c46f44c1c81_1.842853ff2255377a9b83134556f2e39a.jpeg').property('VertexKey', '4GC5MFUS3IC6').property('product_name', 'Kenroy Home 32654 Sisal 1 Light 31\" Tall Table Lamp with Brown Fabric Shade').property('product_type', 'Lamps').property('material', 'Rope').property('name', '4GC5MFUS3IC6').property('finish', 'Rope').property('style', 'Transitional').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/ea40442f-4319-4750-a711-ba2e5d6dd941_1.ed11710424f762d45e5bd33a8531003b.jpeg').property('id', '4GC5MFUS3IC6').property('facet_product_type', 'Lamps').property('home_decor_style', 'Transitional').as('4GC5MFUS3IC6').addV('product').property('product_type', 'Lamps').property('color', 'Silver').property('image_url', 'https://i5.walmartimages.com/asr/6a0b6ef4-92c6-4118-b9cf-d238c657878d_1.1b1b6939091554264dfc68487350f0ca.jpeg').property('VertexKey', '308MDURIJF4G').property('name', '308MDURIJF4G').property('finish', 'Silver').property('id', '308MDURIJF4G').property('facet_product_type', 'Lamps').property('product_name', 'Ashley Saleema Terracotta Table Lamp in Silver').as('308MDURIJF4G').addV('product').property('color', 'Clear').property('shape', 'Drum').property('image_url', 'https://i5.walmartimages.com/asr/a4a25b98-48b3-4b0d-8a00-be491549118b_1.dc5119ea5d60a6047999d4dd4f061a44.jpeg').property('VertexKey', '20BYGVS0W157').property('product_name', 'Signature Design by Ashley L430034 Shanika Collection Table lamp, Transparent').property('product_type', 'Lamps').property('material', 'Glass & Metal').property('secondary_asset_url_3', 'https://i5.walmartimages.com/asr/222db9ee-3b06-4843-993d-c2ca02588b84_1.04104c3b71b32775af3ae42b3b74ca74.jpeg').property('secondary_asset_url_2', 'https://i5.walmartimages.com/asr/84d11d6c-7602-42a6-a7b7-d856e8449ca0_1.24743dbaf5bc44af74412907134daa2c.jpeg').property('name', '20BYGVS0W157').property('finish', 'Clear').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/0b797f65-f2f9-4900-9988-8a80d11b0fbb_1.089918b1b695209f075a95ad31ec30bb.jpeg').property('id', '20BYGVS0W157').property('facet_product_type', 'Lamps').as('20BYGVS0W157').addV('product').property('product_type', 'Lamps').property('color', 'Silver').property('material', 'Steel').property('image_url', 'https://i5.walmartimages.com/asr/a582a4f7-3819-4a17-95b0-93af1cb93926_1.fa86aa8529bf7518012d01b3f36f6035.jpeg').property('VertexKey', '2MLDVD3LPYZN').property('name', '2MLDVD3LPYZN').property('finish', 'Nickel').property('id', '2MLDVD3LPYZN').property('facet_product_type', 'Lamps').property('product_name', 'Elegant Lighting Brio Table Lamp in Vintage Nickel').property('home_decor_style', 'Modern').as('2MLDVD3LPYZN').addV('product').property('product_type', 'Lamps').property('color', 'White').property('secondary_asset_url_2', 'https://i5.walmartimages.com/asr/f4e54722-2201-4ac7-b1f2-24d04bce8ba3_1.889143c117ec2538512ce4623c3067c7.jpeg').property('image_url', 'https://i5.walmartimages.com/asr/46ee764e-a1b4-4b9c-831e-08bf3362b27f_1.889143c117ec2538512ce4623c3067c7.jpeg').property('VertexKey', '1Y30DA5N6CZW').property('name', '1Y30DA5N6CZW').property('finish', 'Painted').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/3dd0c5c2-ec89-47b9-8e8c-f0b8d3853b6c_1.3db644ccff56454290f2981fb0f05605.jpeg').property('id', '1Y30DA5N6CZW').property('facet_product_type', 'Lamps').property('product_name', 'Crown Lighting  1-light Off-white/ Distressed Crackle Glazed Ceramic Handled Jar Table Lamp').as('1Y30DA5N6CZW').addV('product').property('product_type', 'Lamps').property('color', 'Clear').property('image_url', 'https://i5.walmartimages.com/asr/5e894cab-ec78-498c-84a1-9b99d0ceeff9_1.19fa247fe0fe6b7b655646880f39bd12.jpeg').property('VertexKey', '1T3L5VKVODPH').property('name', '1T3L5VKVODPH').property('finish', 'Antique').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/1b298b1d-b572-4dd9-ae52-9d7a6f0a7cf3_1.46c63143dada8809510a825807017fea.jpeg').property('id', '1T3L5VKVODPH').property('facet_product_type', 'Lamps').property('product_name', 'Signature Design by Ashley  Shanika Transparent Glass Table Lamp').as('1T3L5VKVODPH').addV('product').property('product_type', 'Lamps').property('color', 'Brown').property('material', 'Nickle').property('image_url', 'https://i5.walmartimages.com/asr/74daddab-3a6a-4f46-8769-4a76e5553b86_1.bfe9f53c3ea64d17855a02805b53ddea.jpeg').property('VertexKey', '22JDNJJEDD89').property('name', '22JDNJJEDD89').property('finish', 'Brown').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/2ee4ccef-871f-437d-943f-0e491b31dbf7_1.d2e5f17102d490b7c0c2bb0c108871af.jpeg').property('id', '22JDNJJEDD89').property('facet_product_type', 'Lamps').property('product_name', 'Stein World Marina Table Lamp').property('home_decor_style', 'Modern / Contemporary').as('22JDNJJEDD89').addV('product').property('product_type', 'Lamps').property('image_url', 'https://i5.walmartimages.com/asr/002ca222-4591-4fb3-b6a0-3959dfca10e1_1.c225e5ebe5644db693b2e728d868cc28.jpeg').property('VertexKey', '0S7XO1RDPI35').property('name', '0S7XO1RDPI35').property('id', '0S7XO1RDPI35').property('facet_product_type', 'Lamps').property('product_name', 'Loon Peak Pothier Slate Wicker Wireless All-Weather 17.75\\'\\' Table Lamp').as('0S7XO1RDPI35').addV('product').property('product_type', 'Lamps').property('color', 'Off-White').property('material', 'Glass').property('image_url', 'https://i5.walmartimages.com/asr/fe38fa4f-a190-45a9-a0ba-319841bac28b_1.aa78fd03a0e6e4671f2dd2158e824621.jpeg').property('VertexKey', '1ONRDCRN5YUJ').property('name', '1ONRDCRN5YUJ').property('finish', 'Green').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/52cbd621-2bcf-4fc4-b748-22b235a695da_1.4a409cdb9f7e727a235ee61d5ef3f6ae.jpeg').property('id', '1ONRDCRN5YUJ').property('facet_product_type', 'Lamps').property('product_name', 'Stein World Wilson Table Lamp').property('home_decor_style', 'Modern / Contemporary').as('1ONRDCRN5YUJ').addV('product').property('product_type', 'Lamps').property('color', 'Brown').property('image_url', 'https://i5.walmartimages.com/asr/031e5509-6662-40aa-ab7d-3b4d214e518d_1.29de4042f67aed8f7d82148472704aa1.jpeg').property('VertexKey', '6VWLB2FBY00Y').property('name', '6VWLB2FBY00Y').property('finish', 'White').property('id', '6VWLB2FBY00Y').property('facet_product_type', 'Lamps').property('product_name', 'Lamp Factory Birch Wood Knot Painted 23\\'\\' Table Lamp').as('6VWLB2FBY00Y').addV('product').property('product_type', 'Lamps').property('color', 'Gold').property('image_url', 'https://i5.walmartimages.com/asr/2093f1a2-2ca4-4b0f-a99a-ea02a2705039_1.ccf53d9a19e7cb786462d1e7592d195e.jpeg').property('VertexKey', '1WCQXFS6WL6D').property('name', '1WCQXFS6WL6D').property('finish', 'Brown').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/bef93ec0-470d-4963-81dc-3ff4bd1fcce9_1.e6754b84046658bc466cc06a2bc71cc7.jpeg').property('id', '1WCQXFS6WL6D').property('facet_product_type', 'Lamps').property('product_name', 'Table Lamp CYAN DESIGN HILTON 1-Light Golden Crackle Beige Trim Brown Sha CY-929').as('1WCQXFS6WL6D').addV('product').property('product_type', 'Lamps').property('color', 'Brown').property('material', 'Linen').property('image_url', 'https://i5.walmartimages.com/asr/7b120bbb-fd69-430e-9bc9-e597fa14b3ac_1.213cc97b1d41b29fb2a985b676dd3779.jpeg').property('VertexKey', '2I8FDVNP67A0').property('name', '2I8FDVNP67A0').property('finish', 'Sea Foam Green').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/a67dfa3f-7bac-4f9c-a8f6-953ae0cb583b_1.a0b88f22ec86f7c638651c8d6f97fd38.jpeg').property('id', '2I8FDVNP67A0').property('facet_product_type', 'Lamps').property('product_name', 'A Homestead Shoppe Cookie Table Lamp').as('2I8FDVNP67A0').addV('product').property('product_type', 'Lamps').property('color', 'Black').property('image_url', 'http://i5.walmartimages.com/asr/8992bd1b-7f7f-409b-9355-0a42e49f1dcb_1.3d54341f34b9fc00e34916cfee70f8c1.jpeg').property('VertexKey', '7GY13SO5IBJD').property('name', '7GY13SO5IBJD').property('finish', 'Black').property('style', 'Contemporary').property('id', '7GY13SO5IBJD').property('facet_product_type', 'Lamps').property('product_name', 'Lite Source Blakeney 28.5\\'\\' H Table Lamp with Empire Shade').property('home_decor_style', 'Contemporary').as('7GY13SO5IBJD').addV('product').property('product_type', 'Lamps').property('color', 'Beige').property('material', 'Fabric').property('image_url', 'https://i5.walmartimages.com/asr/11a3e7c9-1314-4dd6-affe-bdcc93eb381d_1.e428533f1e483f44e5b1a6da329d4409.jpeg').property('VertexKey', '6VNKJULDN9XD').property('name', '6VNKJULDN9XD').property('finish', 'Clear').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/76c4bf2f-8a17-42b9-839b-182061137952_1.577f0bfa17876123f8adb81caef1526a.jpeg').property('id', '6VNKJULDN9XD').property('facet_product_type', 'Lamps').property('product_name', 'Signature Design by Ashley Shanika Table Lamp').property('home_decor_style', 'Traditional').as('6VNKJULDN9XD').addV('product').property('product_type', 'Lamps').property('color', 'Beige').property('material', 'Ceramic').property('image_url', 'https://i5.walmartimages.com/asr/8e6c6c1d-14df-4dbc-8cd4-e85dbeb3fb64_1.abbc1c8d6d758a665069d80ba987cc54.jpeg').property('VertexKey', '464T6EIRNCN6').property('name', '464T6EIRNCN6').property('finish', 'Brown').property('style', 'Industrial').property('id', '464T6EIRNCN6').property('facet_product_type', 'Lamps').property('product_name', 'Milk Jug Accent Table Lamp').property('home_decor_style', 'Contemporary').as('464T6EIRNCN6').addV('product').property('product_type', 'Lamps').property('image_url', 'https://i5.walmartimages.com/asr/002ca222-4591-4fb3-b6a0-3959dfca10e1_1.c225e5ebe5644db693b2e728d868cc28.jpeg').property('VertexKey', '3CYMVC1ZAD3C').property('name', '3CYMVC1ZAD3C').property('id', '3CYMVC1ZAD3C').property('facet_product_type', 'Lamps').property('product_name', 'Loon Peak Pothier Slate Wicker Wireless All-Weather 17.75\\'\\' Table Lamp').as('3CYMVC1ZAD3C').addV('product').property('product_type', 'Lamps').property('color', 'Clear').property('secondary_asset_url_2', 'https://i5.walmartimages.com/asr/86fe02e7-baf1-4d9f-b3f5-e7090efd1265_1.d5010f4f00493ebb591aab6abd3ebae2.jpeg').property('image_url', 'https://i5.walmartimages.com/asr/ab7da933-7153-4952-b5e2-1436665e3c48_1.681e035b5a617b3be63a2ce292e57de5.jpeg').property('VertexKey', '2R9QL7S8DOV5').property('name', '2R9QL7S8DOV5').property('finish', 'Brushed').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/b2d1d158-34ab-4edb-afb8-0f563a41fd2a_1.f4d126ba77580eadbd2ee1c9b5f95d7e.jpeg').property('id', '2R9QL7S8DOV5').property('facet_product_type', 'Lamps').property('product_name', 'Jeco Inc. 25\\'\\' Table Lamp').as('2R9QL7S8DOV5').addV('product').property('product_type', 'Lamps').property('color', 'Beige').property('material', 'Polyresin').property('image_url', 'https://i5.walmartimages.com/asr/da80aaf6-29a4-4cc5-b383-f6f2443cc609_1.64b8e2ae2d5bf054bcec2fbab55d36be.jpeg').property('VertexKey', '0TC3542MK5S9').property('name', '0TC3542MK5S9').property('id', '0TC3542MK5S9').property('facet_product_type', 'Lamps').property('product_name', 'AHS Lighting L2538SGR-U1 Cookie Sea Foam Green Ceramic Table Lamp with Shade').as('0TC3542MK5S9').addV('product').property('color', 'Brown').property('image_url', 'https://i5.walmartimages.com/asr/0eaab610-17be-4082-98f7-34ee61a97aba_1.03cea5b31c996c02d8fcbda6695bf199.jpeg').property('VertexKey', '5KM6J9YVBMOZ').property('product_name', 'Barnes and Ivy Rustic Table Lamp Hammered Bronze Metal Pot Beige Linen Drum Shade for Living Room Family Bedroom Nightstand').property('product_type', 'Lamps').property('secondary_asset_url_3', 'https://i5.walmartimages.com/asr/c14d6fbf-c967-4874-a0e5-2c33d33ae1db_1.62c2b2e98ed5a882bb53c9a0cbafdc78.jpeg').property('secondary_asset_url_2', 'https://i5.walmartimages.com/asr/a965a232-d96b-4845-8612-8dccd94305ca_1.a98096b68d2cb04d96b97ad440d3cbe7.jpeg').property('name', '5KM6J9YVBMOZ').property('finish', 'Bronze').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/415b2821-b5c1-4189-b8db-5885ee1db1eb_1.67eaf4e0a871678e291c622b11ca9987.jpeg').property('id', '5KM6J9YVBMOZ').property('facet_product_type', 'Lamps').property('home_decor_style', 'Rustic').as('5KM6J9YVBMOZ').addV('product').property('product_type', 'Lamps').property('color', 'Clear').property('image_url', 'http://i5.walmartimages.com/asr/2e21cb55-33be-490a-8592-7f8518738241_1.c26db134aec29a99c3f9a1cb1e28cc49.jpeg').property('VertexKey', '6LDZ4MSPHGYW').property('name', '6LDZ4MSPHGYW').property('id', '6LDZ4MSPHGYW').property('facet_product_type', 'Lamps').property('product_name', 'Ambience  AM 10940  Table Lamps  Lamps  ;Rich Bordeaux').property('home_decor_style', 'Transitional').as('6LDZ4MSPHGYW').addV('product').property('color', 'Beige').property('image_url', 'https://i5.walmartimages.com/asr/a9d5ad7d-dd18-43ac-8aaf-6b7cda6c6fc3_1.04a3420b2f3b1d23615cc24f0d02915f.jpeg').property('VertexKey', '4JBB1THIIKEC').property('product_name', 'Evergreen Enterprises Metal and Glass with Rope Table Lamp').property('product_type', 'Lamps').property('material', 'Iron').property('secondary_asset_url_3', 'https://i5.walmartimages.com/asr/9836bede-5948-41e3-bbf1-df4d136a9e23_1.cde48e3646d6b710b7b5b5225e1d02a8.jpeg').property('secondary_asset_url_2', 'https://i5.walmartimages.com/asr/a03a5c7a-203a-4bc2-828d-4193a74a2934_1.d6b49ad162fa79e492f9fcf31c000ad0.jpeg').property('name', '4JBB1THIIKEC').property('finish', 'Black').property('style', 'Asian Inspired').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/b56a6f38-c37e-444d-bf16-86402d3d4d51_1.d22ba93c98b6a123755c91680996ea6e.jpeg').property('id', '4JBB1THIIKEC').property('facet_product_type', 'Lamps').property('home_decor_style', 'Asian Inspired').as('4JBB1THIIKEC').addV('product').property('product_type', 'Lamps').property('color', 'Brown').property('secondary_asset_url_3', 'https://i5.walmartimages.com/asr/87209737-92cc-49cf-a51c-0a51fa56f4e7_1.bb8308dbab8b9a3832e04c2ff8398e93.jpeg').property('secondary_asset_url_2', 'https://i5.walmartimages.com/asr/c4c7e60b-c665-402e-8208-520e67cef429_1.45bc25dc0acec39dc07af86ffa621d1d.jpeg').property('image_url', 'https://i5.walmartimages.com/asr/942dd6c7-48ce-40e2-8f49-1ae4857cfe2b_1.c6ccac7225c69296db525b0f3de28cdd.jpeg').property('VertexKey', '6782O9ES1FG9').property('name', '6782O9ES1FG9').property('finish', 'White').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/006678da-fb5b-4e5e-81de-95e693438280_1.f159d27f0124b3362ea527fd7c7d106b.jpeg').property('id', '6782O9ES1FG9').property('facet_product_type', 'Lamps').property('product_name', '25\"H Ceramic Ombre Table Lamp - Coffee').as('6782O9ES1FG9').addV('product').property('product_type', 'Lamps').property('image_url', 'http://i5.walmartimages.com/asr/05fec467-1cc2-4c7d-85e8-4a8d5e36a259_1.455d6dd33a04e1df8dde795d494216dd.jpeg').property('VertexKey', '254AL1BGP0V9').property('name', '254AL1BGP0V9').property('finish', 'Gold').property('id', '254AL1BGP0V9').property('facet_product_type', 'Lamps').property('product_name', 'Cyan Designs Hilton Lamp').as('254AL1BGP0V9').addV('product').property('product_type', 'Lamps').property('color', 'Clear').property('image_url', 'https://i5.walmartimages.com/asr/75c953b0-e531-4787-8860-0f7ac5a0ca59_1.9d0c3be622867c105fc45b849090f945.jpeg').property('VertexKey', '50OYPPP4G0W2').property('name', '50OYPPP4G0W2').property('finish', 'Silver').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/492b413a-6585-4855-97da-dd12ecd8e05f_1.919f299a755f53b7dff4214be97197d5.jpeg').property('id', '50OYPPP4G0W2').property('facet_product_type', 'Lamps').property('product_name', 'Convergence Table Lamp-Bulb:Incandescent Bulb').as('50OYPPP4G0W2').addV('product').property('product_type', 'Lamps').property('color', 'White').property('secondary_asset_url_3', 'https://i5.walmartimages.com/asr/6d2469a3-fa10-4b2a-be78-b3c38eb18403_1.3ffd5167cc742654107cc15a76e83e30.jpeg').property('secondary_asset_url_2', 'https://i5.walmartimages.com/asr/9751ea4a-734e-4d1d-bf61-8310fa844915_1.c72bb76086abe5595966c88b7b7b1c01.jpeg').property('image_url', 'https://i5.walmartimages.com/asr/bc8f9369-8115-4d8d-93a5-828d89696119_1.eb182b13657c3d33bc2e414f06bb2a2e.jpeg').property('VertexKey', '4TI1JXG8PJNT').property('name', '4TI1JXG8PJNT').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/753ed279-8114-47ac-a143-61a38ba79b03_1.a576ce08b506bf610e1bde886bb6dd89.jpeg').property('id', '4TI1JXG8PJNT').property('facet_product_type', 'Lamps').property('product_name', 'Aspen Creative 40111-02, Two Pack Set 21 1/2\" High Modern Glass Table Lamp, Clear Seedy Glass Finish with Empire Shaped Lamp Shade in Off White, 12\" Wide').as('4TI1JXG8PJNT').addV('product').property('product_type', 'Lamps').property('color', 'Multicolor').property('image_url', 'https://i5.walmartimages.com/asr/843e55b8-5108-463b-8ae5-ac7ccda53ff7_1.77126c48c26792484aecfe9f67a1069c.jpeg').property('VertexKey', '24OKITATUU2X').property('name', '24OKITATUU2X').property('finish', 'Brown').property('id', '24OKITATUU2X').property('facet_product_type', 'Lamps').property('product_name', 'Kenroy Home 32654ROPE Sisal Table Lamp, Rope').as('24OKITATUU2X').addV('product').property('color', 'Brown').property('shape', 'A19').property('image_url', 'https://i5.walmartimages.com/asr/577ceb97-7093-4c1f-9f60-52ea9b0a7585_1.daf06d90845d7fccbaddc57dd7672fa5.jpeg').property('VertexKey', '1X9VKBNISUP1').property('product_name', 'Crestview Collection Rozy 31\\'\\' Buffet Lamp').property('product_type', 'Lamps').property('material', 'Resin; Fabric').property('name', '1X9VKBNISUP1').property('finish', 'Brown').property('style', 'Industrial').property('id', '1X9VKBNISUP1').property('facet_product_type', 'Lamps').property('home_decor_style', 'Traditional; Transitional').as('1X9VKBNISUP1').addV('product').property('product_type', 'Lamps').property('color', 'Brown').property('image_url', 'https://i5.walmartimages.com/asr/8e04eeb9-060c-4b65-86ae-bf79d1737af2_1.4c990aa87fd9289cf95cf0039d8dc751.jpeg').property('VertexKey', '50G85K19CET0').property('name', '50G85K19CET0').property('finish', 'Bronze/Copper').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/5bfb7876-a43d-4013-a61b-2113a8f3911e_1.e93a7a2571aaf323e47c56be7567ab37.jpeg').property('id', '50G85K19CET0').property('facet_product_type', 'Lamps').property('product_name', 'Kenroy Home Reflection Table Lamp').as('50G85K19CET0').addV('product').property('product_type', 'Lamps').property('color', 'Other').property('material', 'Burlap').property('image_url', 'https://i5.walmartimages.com/asr/ecb02378-7504-45ca-a66c-46265564445e_1.75c6cc020f67933beae56e7a065d8dd7.jpeg').property('VertexKey', '5XJADN2Q0HC3').property('name', '5XJADN2Q0HC3').property('finish', 'Painted').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/82b555ec-818a-44b9-8089-7782c02e3a95_1.0d549672da474838a5ceb283737d3eef.jpeg').property('id', '5XJADN2Q0HC3').property('facet_product_type', 'Lamps').property('product_name', 'A Homestead Shoppe St. Tropez Ivory Table Lamp').property('home_decor_style', 'Traditional').as('5XJADN2Q0HC3').addV('product').property('product_type', 'Lamps').property('color', 'Black').property('shape', 'Square').property('image_url', 'https://i5.walmartimages.com/asr/78306128-f746-4e10-9747-a545218bc80d_1.06551903598310274e3d267c0433ddad.jpeg').property('VertexKey', '4V5V9RH4D04N').property('name', '4V5V9RH4D04N').property('finish', 'Black').property('style', 'Contemporary').property('id', '4V5V9RH4D04N').property('facet_product_type', 'Lamps').property('product_name', 'Lite Source Blakeney Table Lamp').property('home_decor_style', 'Modern / Contemporary').as('4V5V9RH4D04N').addV('product').property('product_type', 'Lamps').property('color', 'Black').property('material', 'Ceramic').property('image_url', 'https://i5.walmartimages.com/asr/e717eb35-3ac7-4310-8e42-f62f87d1f482_1.3067abd90802bbf8ff3ab6e890077081.jpeg').property('VertexKey', '5GCCZEZ4H9KM').property('name', '5GCCZEZ4H9KM').property('finish', 'Black').property('secondary_asset_url_1', 'https://i5.walmartimages.com/asr/0a80e2dc-4731-4c04-88de-ba14d7f7ff14_1.1782683979d11f63a0c9795968f97676.jpeg').property('id', '5GCCZEZ4H9KM').property('facet_product_type', 'Lamps').property('product_name', 'Stein World Metal Table Lamp').property('home_decor_style', 'Traditional').as('5GCCZEZ4H9KM').V('4DKFHKLWMZIC').has('VertexKey', '4DKFHKLWMZIC').as('4DKFHKLWMZIC').V('4BLKBEVWAPP0').has('VertexKey', '4BLKBEVWAPP0').as('4BLKBEVWAPP0').V('4EWTQJUPJS1S').has('VertexKey', '4EWTQJUPJS1S').as('4EWTQJUPJS1S').V('1H0A6GMRBS48').has('VertexKey', '1H0A6GMRBS48').as('1H0A6GMRBS48').V('5JPAP4IVTZLU').has('VertexKey', '5JPAP4IVTZLU').as('5JPAP4IVTZLU').V('612GAUDUEKYQ').has('VertexKey', '612GAUDUEKYQ').as('612GAUDUEKYQ').V('5EVD9JLJCOAB').has('VertexKey', '5EVD9JLJCOAB').as('5EVD9JLJCOAB').V('1TJWVSFXHYLR').has('VertexKey', '1TJWVSFXHYLR').as('1TJWVSFXHYLR').V('5J9V2I0NV3Z7').has('VertexKey', '5J9V2I0NV3Z7').as('5J9V2I0NV3Z7').V('6BWTJ6EQ9J8N').has('VertexKey', '6BWTJ6EQ9J8N').as('6BWTJ6EQ9J8N').V('6B46RP7GSJIF').has('VertexKey', '6B46RP7GSJIF').as('6B46RP7GSJIF').V('1LIG3BROLNG4').has('VertexKey', '1LIG3BROLNG4').as('1LIG3BROLNG4').V('1XF76DBHBZ4W').has('VertexKey', '1XF76DBHBZ4W').as('1XF76DBHBZ4W').V('4DNVWHXO3GAP').has('VertexKey', '4DNVWHXO3GAP').as('4DNVWHXO3GAP').V('4S7K07HB8NBA').has('VertexKey', '4S7K07HB8NBA').as('4S7K07HB8NBA').V('1CYK1R0DLM3I').has('VertexKey', '1CYK1R0DLM3I').as('1CYK1R0DLM3I').V('1HSVFWCHHBE7').has('VertexKey', '1HSVFWCHHBE7').as('1HSVFWCHHBE7').V('3WR2HNMZB91X').has('VertexKey', '3WR2HNMZB91X').as('3WR2HNMZB91X').V('69CCYEIMKL3B').has('VertexKey', '69CCYEIMKL3B').as('69CCYEIMKL3B').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_308MDURIJF4G').from('4GC5MFUS3IC6').to('308MDURIJF4G').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_4DKFHKLWMZIC').from('4GC5MFUS3IC6').to('4DKFHKLWMZIC').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_20BYGVS0W157').from('4GC5MFUS3IC6').to('20BYGVS0W157').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_4BLKBEVWAPP0').from('4GC5MFUS3IC6').to('4BLKBEVWAPP0').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_2MLDVD3LPYZN').from('4GC5MFUS3IC6').to('2MLDVD3LPYZN').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_1Y30DA5N6CZW').from('4GC5MFUS3IC6').to('1Y30DA5N6CZW').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_4EWTQJUPJS1S').from('4GC5MFUS3IC6').to('4EWTQJUPJS1S').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_1T3L5VKVODPH').from('4GC5MFUS3IC6').to('1T3L5VKVODPH').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_22JDNJJEDD89').from('4GC5MFUS3IC6').to('22JDNJJEDD89').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_0S7XO1RDPI35').from('4GC5MFUS3IC6').to('0S7XO1RDPI35').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_1ONRDCRN5YUJ').from('4GC5MFUS3IC6').to('1ONRDCRN5YUJ').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_1H0A6GMRBS48').from('4GC5MFUS3IC6').to('1H0A6GMRBS48').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_6VWLB2FBY00Y').from('4GC5MFUS3IC6').to('6VWLB2FBY00Y').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_5JPAP4IVTZLU').from('4GC5MFUS3IC6').to('5JPAP4IVTZLU').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_612GAUDUEKYQ').from('4GC5MFUS3IC6').to('612GAUDUEKYQ').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_5EVD9JLJCOAB').from('4GC5MFUS3IC6').to('5EVD9JLJCOAB').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_1WCQXFS6WL6D').from('4GC5MFUS3IC6').to('1WCQXFS6WL6D').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_2I8FDVNP67A0').from('4GC5MFUS3IC6').to('2I8FDVNP67A0').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_7GY13SO5IBJD').from('4GC5MFUS3IC6').to('7GY13SO5IBJD').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_1TJWVSFXHYLR').from('4GC5MFUS3IC6').to('1TJWVSFXHYLR').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_5J9V2I0NV3Z7').from('4GC5MFUS3IC6').to('5J9V2I0NV3Z7').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_6VNKJULDN9XD').from('4GC5MFUS3IC6').to('6VNKJULDN9XD').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_6BWTJ6EQ9J8N').from('4GC5MFUS3IC6').to('6BWTJ6EQ9J8N').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_6B46RP7GSJIF').from('4GC5MFUS3IC6').to('6B46RP7GSJIF').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_464T6EIRNCN6').from('4GC5MFUS3IC6').to('464T6EIRNCN6').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_1LIG3BROLNG4').from('4GC5MFUS3IC6').to('1LIG3BROLNG4').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_3CYMVC1ZAD3C').from('4GC5MFUS3IC6').to('3CYMVC1ZAD3C').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_2R9QL7S8DOV5').from('4GC5MFUS3IC6').to('2R9QL7S8DOV5').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_1XF76DBHBZ4W').from('4GC5MFUS3IC6').to('1XF76DBHBZ4W').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_4DNVWHXO3GAP').from('4GC5MFUS3IC6').to('4DNVWHXO3GAP').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_0TC3542MK5S9').from('4GC5MFUS3IC6').to('0TC3542MK5S9').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_5KM6J9YVBMOZ').from('4GC5MFUS3IC6').to('5KM6J9YVBMOZ').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_6LDZ4MSPHGYW').from('4GC5MFUS3IC6').to('6LDZ4MSPHGYW').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_4JBB1THIIKEC').from('4GC5MFUS3IC6').to('4JBB1THIIKEC').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_4S7K07HB8NBA').from('4GC5MFUS3IC6').to('4S7K07HB8NBA').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_1CYK1R0DLM3I').from('4GC5MFUS3IC6').to('1CYK1R0DLM3I').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_6782O9ES1FG9').from('4GC5MFUS3IC6').to('6782O9ES1FG9').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_254AL1BGP0V9').from('4GC5MFUS3IC6').to('254AL1BGP0V9').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_50OYPPP4G0W2').from('4GC5MFUS3IC6').to('50OYPPP4G0W2').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_1HSVFWCHHBE7').from('4GC5MFUS3IC6').to('1HSVFWCHHBE7').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_4TI1JXG8PJNT').from('4GC5MFUS3IC6').to('4TI1JXG8PJNT').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_24OKITATUU2X').from('4GC5MFUS3IC6').to('24OKITATUU2X').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_1X9VKBNISUP1').from('4GC5MFUS3IC6').to('1X9VKBNISUP1').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_50G85K19CET0').from('4GC5MFUS3IC6').to('50G85K19CET0').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_5XJADN2Q0HC3').from('4GC5MFUS3IC6').to('5XJADN2Q0HC3').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_4V5V9RH4D04N').from('4GC5MFUS3IC6').to('4V5V9RH4D04N').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_3WR2HNMZB91X').from('4GC5MFUS3IC6').to('3WR2HNMZB91X').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_5GCCZEZ4H9KM').from('4GC5MFUS3IC6').to('5GCCZEZ4H9KM').addE('substitute').property('id', '4GC5MFUS3IC6_substitute_69CCYEIMKL3B').from('4GC5MFUS3IC6').to('69CCYEIMKL3B')" };

    private static String[] PRECREATE_VERTEX_IDS = new String[]
            {"4DKFHKLWMZIC","4BLKBEVWAPP0","4EWTQJUPJS1S","1H0A6GMRBS48","5JPAP4IVTZLU","612GAUDUEKYQ","5EVD9JLJCOAB","1TJWVSFXHYLR","5J9V2I0NV3Z7","6BWTJ6EQ9J8N","6B46RP7GSJIF","1LIG3BROLNG4","1XF76DBHBZ4W","4DNVWHXO3GAP","4S7K07HB8NBA","1CYK1R0DLM3I","1HSVFWCHHBE7","3WR2HNMZB91X","69CCYEIMKL3B"};

    private static String[] VERTEX_IDS = new String[]
            {"4GC5MFUS3IC6", "6VWLB2FBY00Y","464T6EIRNCN6","6782O9ES1FG9","1X9VKBNISUP1","50G85K19CET0","2R9QL7S8DOV5","308MDURIJF4G","1T3L5VKVODPH","2I8FDVNP67A0","6VNKJULDN9XD","0TC3542MK5S9","5XJADN2Q0HC3","20BYGVS0W157","1ONRDCRN5YUJ","6LDZ4MSPHGYW","254AL1BGP0V9","24OKITATUU2X","2MLDVD3LPYZN","1Y30DA5N6CZW","22JDNJJEDD89","5GCCZEZ4H9KM","5KM6J9YVBMOZ","50OYPPP4G0W2","4V5V9RH4D04N","0S7XO1RDPI35","1WCQXFS6WL6D","7GY13SO5IBJD","3CYMVC1ZAD3C","4JBB1THIIKEC","4TI1JXG8PJNT"};

/*
    private final String[] partialUpdateQueries = new String[] {
            "g.V('4GC5MFUS3IC6').has('VertexKey', '4GC5MFUS3IC6').outE('substitute').drop()",
            insertQueries[2],
    };
*/

    public static String HOST = "https://<account>.documents.azure.com:443/";
    public static String KEY  = "<key>";
    public static String DATABASE_ID = "<db>";
    public static String COLLECTION_ID = "<collection>";

    public static void main( String[] args ) throws ExecutionException, InterruptedException, DocumentClientException {
        new Main().run();
    }

    private void run() throws DocumentClientException, ExecutionException, InterruptedException {
        System.out.println("Testing using gremlin insert queries.... ");
        testGremlin(insertQueries);
        System.out.println();
        System.out.println();
        System.out.println("Testing full insert using bulk executor .... ");
        testBulkExecutor(false);
        System.out.println();
        System.out.println();
        System.out.println("Testing substitute edges drop and upsert using bulk executor.... ");
        testBulkExecutor(true);
        System.out.println();
        System.out.println();
        System.out.println("Testing update substitute edges using bulk executor.... ");
        testBulkUpdate();
        System.out.println();
        System.out.println();

    }

    private void testGremlin(String[] queries) throws ExecutionException, InterruptedException {
        /**
         * There typically needs to be only one Cluster instance in an application.
         */
        Cluster cluster;

        /**
         * Use the Cluster instance to construct different Client instances (e.g. one for sessionless communication
         * and one or more sessions). A sessionless Client should be thread-safe and typically no more than one is
         * needed unless there is some need to divide connection pools across multiple Client instances. In this case
         * there is just a single sessionless Client instance used for the entire App.
         */
        Client client;

        try {
            // Attempt to create the connection objects
            cluster = Cluster.build(new File("src/remote.yaml")).create();
            client = cluster.connect();
        } catch (FileNotFoundException e) {
            // Handle file errors.
            System.out.println("Couldn't find the configuration file.");
            e.printStackTrace();
            return;
        }

        // After connection is successful, run all the queries against the server.
        for (String query : queries) {
            System.out.println("\nSubmitting this Gremlin query: " + query);

            // Submitting remote query to the server.
            Stopwatch watch = Stopwatch.createStarted();
            ResultSet results = client.submit(query);

            CompletableFuture<List<Result>> completableFutureResults = results.all();
            List<Result> resultList = completableFutureResults.get();

            for (Result result : resultList) {
                System.out.println("\nQuery result:");
                System.out.println(result.toString());
            }

            watch.stop();
            System.out.println("Total request charge = " + results.statusAttributes().get().get("x-ms-total-request-charge"));
            System.out.println("Latency = " + watch.elapsed().toMillis());
        }

        cluster.close();
    }

    private void testBulkUpdate() throws DocumentClientException {
        ConnectionPolicy directModePolicy = new ConnectionPolicy();
        directModePolicy.setConnectionMode(ConnectionMode.DirectHttps);
        DocumentClient client = new DocumentClient(HOST, KEY, directModePolicy, ConsistencyLevel.Eventual);
        DocumentCollection collection = client.readCollection(String.format("/dbs/%s/colls/%s", DATABASE_ID, COLLECTION_ID), null).getResource();
        GraphBulkExecutor.Builder graphBulkExecutorBuilder = GraphBulkExecutor.builder()
                .from(client, DATABASE_ID, COLLECTION_ID, collection.getPartitionKey(), getOfferThroughput(client, collection));

        try (GraphBulkExecutor executor = graphBulkExecutorBuilder.build()) {

            BulkUpdateResponse updateResponse = executor.updateAll(readEdges("edges.graphson").stream().map((e) -> {
                e.addProperty("weight", 1.0);
                return e;
            }).collect(Collectors.toList()), 20);

            System.out.println(String.format("Finished update with RUSs: %s, latency: %s, total items updated: %s, errors: %s",
                    updateResponse.getTotalRequestUnitsConsumed(),
                    updateResponse.getTotalTimeTaken(),
                    updateResponse.getNumberOfDocumentsUpdated(),
                    updateResponse.getErrors()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        client.close();
    }

    private void testBulkExecutor(Boolean deleteEdgesOnly) throws DocumentClientException {
        ConnectionPolicy directModePolicy = new ConnectionPolicy();
        directModePolicy.setConnectionMode(ConnectionMode.DirectHttps);
        DocumentClient client = new DocumentClient(HOST, KEY, directModePolicy, ConsistencyLevel.Eventual);
        DocumentCollection collection = client.readCollection(String.format("/dbs/%s/colls/%s", DATABASE_ID, COLLECTION_ID), null).getResource();
        GraphBulkExecutor.Builder graphBulkExecutorBuilder = GraphBulkExecutor.builder()
                .from(client, DATABASE_ID, COLLECTION_ID, collection.getPartitionKey(), getOfferThroughput(client, collection));

        try (GraphBulkExecutor executor = graphBulkExecutorBuilder.build()) {

            if (deleteEdgesOnly) {
                BulkDeleteResponse response = executor.deleteEdgesByLabel(VERTEX_IDS[0], "substitute");

                System.out.println(String.format("Finished edge delete on vertex: %s, label: %s ; RUSs: %s, latency: %s, total items: %s, errors: %s",
                        VERTEX_IDS[0],
                        "substitute",
                        response.getTotalRequestUnitsConsumed(),
                        response.getTotalTimeTaken().toMillis(),
                        response.getNumberOfDocumentsDeleted(),
                        response.getErrors()));
            }
            else {
                BulkDeleteResponse response = executor.deleteAll();
                System.out.println(String.format("Deleted all with total of %s object, total RUS: %s and latency: %s, errors: %s",
                        response.getNumberOfDocumentsDeleted(),
                        response.getTotalRequestUnitsConsumed(),
                        response.getTotalTimeTaken().toMillis(),
                        response.getErrors()));

                // Pre-load
                BulkImportResponse preLoadVertices = executor.importAll(generateVertices(PRECREATE_VERTEX_IDS), true,true, 20);

                System.out.println(String.format("Finished data pre-load with RUSs: %s, latency: %s, total vertices: %s, errors: %s",
                        preLoadVertices.getTotalRequestUnitsConsumed(),
                        preLoadVertices.getTotalTimeTaken().toMillis(),
                        preLoadVertices.getNumberOfDocumentsImported(),
                        preLoadVertices.getErrors()));
            }

            List<GremlinVertex> vertices = readVertices("vertices.graphson");
            BulkImportResponse vResponse = executor.importAll(vertices, true, true, 20);

            List<GremlinEdge> edges = readEdges("edges.graphson");
            BulkImportResponse eResponse = executor.importAll(edges, true, true, 20);

            System.out.println(String.format("Finished ingestion with RUSs: %s, latency: %s, total vertex %s, total edges %s, errors: %s",
                    vResponse.getTotalRequestUnitsConsumed() + eResponse.getTotalRequestUnitsConsumed(),
                    vResponse.getTotalTimeTaken().toMillis() + eResponse.getTotalTimeTaken().toMillis(),
                    vResponse.getNumberOfDocumentsImported(),
                    eResponse.getNumberOfDocumentsImported(),
                    new ArrayList<>(vResponse.getErrors()).addAll(eResponse.getErrors())));

        } catch (Exception e) {
            e.printStackTrace();
        }

        client.close();
    }

    private static int getOfferThroughput(DocumentClient client, DocumentCollection collection) {
        FeedResponse<Offer> offers = client.queryOffers(String.format("SELECT * FROM c where c.offerResourceId = '%s'", collection.getResourceId()), null);

        List<Offer> offerAsList = offers.getQueryIterable().toList();
        if (offerAsList.isEmpty()) {
            throw new IllegalStateException("Cannot find Collection's corresponding offer");
        }

        Offer offer = offerAsList.get(0);
        return offer.getContent().getInt("offerThroughput");
    }

    private List<GremlinEdge> generateEdges(String srcVertexId, String[] vertexIds, int startIndex)
    {
        List<GremlinEdge> edges = new ArrayList<>();
        for (int i = startIndex; i < vertexIds.length; i++)
        {
            GremlinEdge e1 = new GremlinEdge(
                String.format("%s_%s_%s", srcVertexId, "substitute", vertexIds[i]),
                "substitute",
                    srcVertexId,
                    vertexIds[i],
                "product",
                "product",
                    srcVertexId,
                    vertexIds[i]);

            edges.add(e1);
        }

        return edges;
    }

    private List<GremlinVertex> generateVertices(String[] vertexIds)
    {
        List<GremlinVertex> vertices = new ArrayList<>();

        for (String vertexId: Arrays.asList(vertexIds))
        {
            GremlinVertex v = new GremlinVertex(vertexId, "product");
            v.addProperty("product_type", UUID.randomUUID().toString());
            v.addProperty("color", UUID.randomUUID().toString());
            v.addProperty("image_url", UUID.randomUUID().toString());
            v.addProperty("VertexKey", vertexId);
            v.addProperty("finish", UUID.randomUUID().toString());
            v.addProperty("name", UUID.randomUUID().toString());
            v.addProperty("facet_product_type", UUID.randomUUID().toString());
            v.addProperty("product_name", UUID.randomUUID().toString());

            vertices.add(v);
        }

        return vertices;
    }

    private void saveGraphResources(Client client) throws InterruptedException, ExecutionException {
        ResultSet resultList = client.submit("g.V()");
        saveToFile(resultList.all().get(), "vertices.json");

        resultList = client.submit("g.E()");
        saveToFile(resultList.all().get(), "edges.json");
    }

    private void saveToFile(List<Result> vertexResultList, String fileName) {
        try {
            try (FileOutputStream stream =
                         new FileOutputStream(fileName);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
            ) {
                for (Result result : vertexResultList) {
                    GraphSONIo.build(GraphSONVersion.V1_0).graph(EmptyGraph.instance()).create().writer().create().writeObject(stream, result.getObject());
                    stream.flush();
                    writer.newLine();
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<GremlinVertex> readVertices(String fileName)
    {
        List<GremlinVertex> batchVertices = new ArrayList<GremlinVertex>();

        try (FileInputStream stream = new FileInputStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {

            GraphReader graphReader = GraphSONIo.build(GraphSONVersion.V1_0).graph(EmptyGraph.instance()).create().reader().create();

            String line = reader.readLine();
            while (line != null) {
                Vertex vertex = graphReader.readVertex(new ByteArrayInputStream(line.getBytes()), (a) -> a.get());

                GremlinVertex batchVertex = new GremlinVertex(vertex.id().toString(), vertex.label());

                vertex.properties().forEachRemaining((t) -> {
                    batchVertex.addProperty(t.label(), t.value());
                });

                batchVertices.add(batchVertex);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return batchVertices;
    }

    private List<GremlinEdge> readEdges(String fileName)
    {
        List<GremlinEdge> batchEdges = new ArrayList<GremlinEdge>();

        try (FileInputStream stream = new FileInputStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {

            GraphReader graphReader = GraphSONIo.build(GraphSONVersion.V1_0).graph(EmptyGraph.instance()).create().reader().create();

            String line = reader.readLine();
            while (line != null) {
                Edge edge = graphReader.readEdge(new ByteArrayInputStream(line.getBytes()), (a) -> a.get());

                GremlinEdge batchEdge = new GremlinEdge(
                        edge.id().toString(),
                        edge.label(),
                        edge.outVertex().id().toString(),
                        edge.inVertex().id().toString(),
                        edge.outVertex().label(),
                        edge.inVertex().label(),
                        edge.outVertex().id(),
                        edge.inVertex().id());

                edge.properties().forEachRemaining((t) -> {
                    batchEdge.addProperty(t.key(), t.value());
                });

                batchEdges.add(batchEdge);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return batchEdges;
    }
}