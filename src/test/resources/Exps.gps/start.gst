<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="Exps_start" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>382 858 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:InitialNode</string>
            </attr>
        </edge>
        <node id="n1">
            <attr name="layout">
                <string>62 778 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:ActivityDiagram</string>
            </attr>
        </edge>
        <node id="n41">
            <attr name="layout">
                <string>382 698 0 0</string>
            </attr>
        </node>
        <edge from="n41" to="n41">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n1" to="n41">
            <attr name="label">
                <string>running</string>
            </attr>
        </edge>
        <node id="n42">
            <attr name="layout">
                <string>382 778 0 0</string>
            </attr>
        </node>
        <edge from="n42" to="n42">
            <attr name="label">
                <string>string:"Exps"</string>
            </attr>
        </edge>
        <edge from="n1" to="n42">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>1087 720 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n43">
            <attr name="layout">
                <string>1302 712 0 0</string>
            </attr>
        </node>
        <edge from="n43" to="n43">
            <attr name="label">
                <string>string:"x"</string>
            </attr>
        </edge>
        <edge from="n2" to="n43">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>1377 792 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:IntegerValue</string>
            </attr>
        </edge>
        <node id="n44">
            <attr name="layout">
                <string>1727 792 0 0</string>
            </attr>
        </node>
        <edge from="n44" to="n44">
            <attr name="label">
                <string>int:1</string>
            </attr>
        </edge>
        <edge from="n3" to="n44">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>1087 80 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n45">
            <attr name="layout">
                <string>1302 72 0 0</string>
            </attr>
        </node>
        <edge from="n45" to="n45">
            <attr name="label">
                <string>string:"y"</string>
            </attr>
        </edge>
        <edge from="n4" to="n45">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>1377 152 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:IntegerValue</string>
            </attr>
        </edge>
        <node id="n46">
            <attr name="layout">
                <string>1727 152 0 0</string>
            </attr>
        </node>
        <edge from="n46" to="n46">
            <attr name="label">
                <string>int:2</string>
            </attr>
        </edge>
        <edge from="n5" to="n46">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>1087 1680 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n47">
            <attr name="layout">
                <string>1302 1752 0 0</string>
            </attr>
        </node>
        <edge from="n47" to="n47">
            <attr name="label">
                <string>string:"A"</string>
            </attr>
        </edge>
        <edge from="n6" to="n47">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n7">
            <attr name="layout">
                <string>1377 1672 0 0</string>
            </attr>
        </node>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n48">
            <attr name="layout">
                <string>1727 1672 0 0</string>
            </attr>
        </node>
        <edge from="n48" to="n48">
            <attr name="label">
                <string>bool:true</string>
            </attr>
        </edge>
        <edge from="n7" to="n48">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n8">
            <attr name="layout">
                <string>1087 1360 0 0</string>
            </attr>
        </node>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n49">
            <attr name="layout">
                <string>1302 1352 0 0</string>
            </attr>
        </node>
        <edge from="n49" to="n49">
            <attr name="label">
                <string>string:"Not A"</string>
            </attr>
        </edge>
        <edge from="n8" to="n49">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n9">
            <attr name="layout">
                <string>1377 1432 0 0</string>
            </attr>
        </node>
        <edge from="n9" to="n9">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n50">
            <attr name="layout">
                <string>1727 1432 0 0</string>
            </attr>
        </node>
        <edge from="n50" to="n50">
            <attr name="label">
                <string>bool:true</string>
            </attr>
        </edge>
        <edge from="n9" to="n50">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n10">
            <attr name="layout">
                <string>1087 1840 0 0</string>
            </attr>
        </node>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n51">
            <attr name="layout">
                <string>1302 1832 0 0</string>
            </attr>
        </node>
        <edge from="n51" to="n51">
            <attr name="label">
                <string>string:"B"</string>
            </attr>
        </edge>
        <edge from="n10" to="n51">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n11">
            <attr name="layout">
                <string>1377 1912 0 0</string>
            </attr>
        </node>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n52">
            <attr name="layout">
                <string>1727 1912 0 0</string>
            </attr>
        </node>
        <edge from="n52" to="n52">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n11" to="n52">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n12">
            <attr name="layout">
                <string>1087 1520 0 0</string>
            </attr>
        </node>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n53">
            <attr name="layout">
                <string>1302 1592 0 0</string>
            </attr>
        </node>
        <edge from="n53" to="n53">
            <attr name="label">
                <string>string:"A and B"</string>
            </attr>
        </edge>
        <edge from="n12" to="n53">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n13">
            <attr name="layout">
                <string>1377 1512 0 0</string>
            </attr>
        </node>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n54">
            <attr name="layout">
                <string>1727 1512 0 0</string>
            </attr>
        </node>
        <edge from="n54" to="n54">
            <attr name="label">
                <string>bool:true</string>
            </attr>
        </edge>
        <edge from="n13" to="n54">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n14">
            <attr name="layout">
                <string>1087 400 0 0</string>
            </attr>
        </node>
        <edge from="n14" to="n14">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n55">
            <attr name="layout">
                <string>1302 472 0 0</string>
            </attr>
        </node>
        <edge from="n55" to="n55">
            <attr name="label">
                <string>string:"x equals x"</string>
            </attr>
        </edge>
        <edge from="n14" to="n55">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n15">
            <attr name="layout">
                <string>1377 392 0 0</string>
            </attr>
        </node>
        <edge from="n15" to="n15">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n56">
            <attr name="layout">
                <string>1727 392 0 0</string>
            </attr>
        </node>
        <edge from="n56" to="n56">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n15" to="n56">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n16">
            <attr name="layout">
                <string>1087 1200 0 0</string>
            </attr>
        </node>
        <edge from="n16" to="n16">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n57">
            <attr name="layout">
                <string>1302 1192 0 0</string>
            </attr>
        </node>
        <edge from="n57" to="n57">
            <attr name="label">
                <string>string:"x greater equals x"</string>
            </attr>
        </edge>
        <edge from="n16" to="n57">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n17">
            <attr name="layout">
                <string>1377 1272 0 0</string>
            </attr>
        </node>
        <edge from="n17" to="n17">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n58">
            <attr name="layout">
                <string>1727 1272 0 0</string>
            </attr>
        </node>
        <edge from="n58" to="n58">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n17" to="n58">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n18">
            <attr name="layout">
                <string>1087 560 0 0</string>
            </attr>
        </node>
        <edge from="n18" to="n18">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n59">
            <attr name="layout">
                <string>1302 552 0 0</string>
            </attr>
        </node>
        <edge from="n59" to="n59">
            <attr name="label">
                <string>string:"x greater x"</string>
            </attr>
        </edge>
        <edge from="n18" to="n59">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n19">
            <attr name="layout">
                <string>1377 632 0 0</string>
            </attr>
        </node>
        <edge from="n19" to="n19">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n60">
            <attr name="layout">
                <string>1727 632 0 0</string>
            </attr>
        </node>
        <edge from="n60" to="n60">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n19" to="n60">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n20">
            <attr name="layout">
                <string>1087 1040 0 0</string>
            </attr>
        </node>
        <edge from="n20" to="n20">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n61">
            <attr name="layout">
                <string>1302 1112 0 0</string>
            </attr>
        </node>
        <edge from="n61" to="n61">
            <attr name="label">
                <string>string:"x smaller x"</string>
            </attr>
        </edge>
        <edge from="n20" to="n61">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n21">
            <attr name="layout">
                <string>1377 1032 0 0</string>
            </attr>
        </node>
        <edge from="n21" to="n21">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n62">
            <attr name="layout">
                <string>1727 1032 0 0</string>
            </attr>
        </node>
        <edge from="n62" to="n62">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n21" to="n62">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n22">
            <attr name="layout">
                <string>1087 880 0 0</string>
            </attr>
        </node>
        <edge from="n22" to="n22">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n63">
            <attr name="layout">
                <string>1302 952 0 0</string>
            </attr>
        </node>
        <edge from="n63" to="n63">
            <attr name="label">
                <string>string:"x smaller equals x"</string>
            </attr>
        </edge>
        <edge from="n22" to="n63">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n23">
            <attr name="layout">
                <string>1377 872 0 0</string>
            </attr>
        </node>
        <edge from="n23" to="n23">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n64">
            <attr name="layout">
                <string>1727 872 0 0</string>
            </attr>
        </node>
        <edge from="n64" to="n64">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n23" to="n64">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n24">
            <attr name="layout">
                <string>1087 2000 0 0</string>
            </attr>
        </node>
        <edge from="n24" to="n24">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n65">
            <attr name="layout">
                <string>1302 1992 0 0</string>
            </attr>
        </node>
        <edge from="n65" to="n65">
            <attr name="label">
                <string>string:"A or B"</string>
            </attr>
        </edge>
        <edge from="n24" to="n65">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n25">
            <attr name="layout">
                <string>1377 2072 0 0</string>
            </attr>
        </node>
        <edge from="n25" to="n25">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n66">
            <attr name="layout">
                <string>1727 2072 0 0</string>
            </attr>
        </node>
        <edge from="n66" to="n66">
            <attr name="label">
                <string>bool:true</string>
            </attr>
        </edge>
        <edge from="n25" to="n66">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n26">
            <attr name="layout">
                <string>1087 240 0 0</string>
            </attr>
        </node>
        <edge from="n26" to="n26">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n67">
            <attr name="layout">
                <string>1302 312 0 0</string>
            </attr>
        </node>
        <edge from="n67" to="n67">
            <attr name="label">
                <string>string:"diff"</string>
            </attr>
        </edge>
        <edge from="n26" to="n67">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n27">
            <attr name="layout">
                <string>1377 232 0 0</string>
            </attr>
        </node>
        <edge from="n27" to="n27">
            <attr name="label">
                <string>type:IntegerValue</string>
            </attr>
        </edge>
        <node id="n68">
            <attr name="layout">
                <string>1727 232 0 0</string>
            </attr>
        </node>
        <edge from="n68" to="n68">
            <attr name="label">
                <string>int:0</string>
            </attr>
        </edge>
        <edge from="n27" to="n68">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n28">
            <attr name="layout">
                <string>382 938 0 0</string>
            </attr>
        </node>
        <edge from="n28" to="n28">
            <attr name="label">
                <string>type:OpaqueAction</string>
            </attr>
        </edge>
        <node id="n69">
            <attr name="layout">
                <string>697 1760 0 0</string>
            </attr>
        </node>
        <edge from="n69" to="n69">
            <attr name="label">
                <string>string:"Action1"</string>
            </attr>
        </edge>
        <edge from="n28" to="n69">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n29">
            <attr name="layout">
                <string>776 695 0 0</string>
            </attr>
        </node>
        <edge from="n29" to="n29">
            <attr name="label">
                <string>type:Equals</string>
            </attr>
        </edge>
        <node id="n30">
            <attr name="layout">
                <string>731 240 0 0</string>
            </attr>
        </node>
        <edge from="n30" to="n30">
            <attr name="label">
                <string>type:Difference</string>
            </attr>
        </edge>
        <node id="n31">
            <attr name="layout">
                <string>797 1369 0 0</string>
            </attr>
        </node>
        <edge from="n31" to="n31">
            <attr name="label">
                <string>type:Not</string>
            </attr>
        </edge>
        <node id="n32">
            <attr name="layout">
                <string>810 1680 0 0</string>
            </attr>
        </node>
        <edge from="n32" to="n32">
            <attr name="label">
                <string>type:And</string>
            </attr>
        </edge>
        <node id="n33">
            <attr name="layout">
                <string>821 1849 0 0</string>
            </attr>
        </node>
        <edge from="n33" to="n33">
            <attr name="label">
                <string>type:Or</string>
            </attr>
        </edge>
        <node id="n34">
            <attr name="layout">
                <string>697 855 0 0</string>
            </attr>
        </node>
        <edge from="n34" to="n34">
            <attr name="label">
                <string>type:SmallerEquals</string>
            </attr>
        </edge>
        <node id="n35">
            <attr name="layout">
                <string>765 935 0 0</string>
            </attr>
        </node>
        <edge from="n35" to="n35">
            <attr name="label">
                <string>type:Smaller</string>
            </attr>
        </edge>
        <node id="n36">
            <attr name="layout">
                <string>697 1015 0 0</string>
            </attr>
        </node>
        <edge from="n36" to="n36">
            <attr name="label">
                <string>type:GreaterEquals</string>
            </attr>
        </edge>
        <node id="n37">
            <attr name="layout">
                <string>765 775 0 0</string>
            </attr>
        </node>
        <edge from="n37" to="n37">
            <attr name="label">
                <string>type:Greater</string>
            </attr>
        </edge>
        <node id="n38">
            <attr name="layout">
                <string>382 1018 0 0</string>
            </attr>
        </node>
        <edge from="n38" to="n38">
            <attr name="label">
                <string>type:FinalNode</string>
            </attr>
        </edge>
        <node id="n39">
            <attr name="layout">
                <string>122 874 0 0</string>
            </attr>
        </node>
        <edge from="n39" to="n39">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <node id="n40">
            <attr name="layout">
                <string>122 954 0 0</string>
            </attr>
        </node>
        <edge from="n40" to="n40">
            <attr name="label">
                <string>type:ControlFlow</string>
            </attr>
        </edge>
        <edge from="n1" to="n0">
            <attr name="label">
                <string>start</string>
            </attr>
        </edge>
        <edge from="n2" to="n3">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n4" to="n5">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n6" to="n7">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n8" to="n9">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n10" to="n11">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n12" to="n13">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n14" to="n15">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n16" to="n17">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n18" to="n19">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n20" to="n21">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n22" to="n23">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n24" to="n25">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n26" to="n27">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n28" to="n29">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n29" to="n14">
            <attr name="label">
                <string>assignee</string>
            </attr>
        </edge>
        <edge from="n29" to="n2">
            <attr name="label">
                <string>1</string>
            </attr>
        </edge>
        <edge from="n29" to="n2">
            <attr name="label">
                <string>2</string>
            </attr>
        </edge>
        <edge from="n28" to="n30">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n30" to="n26">
            <attr name="label">
                <string>assignee</string>
            </attr>
        </edge>
        <edge from="n30" to="n2">
            <attr name="label">
                <string>1</string>
            </attr>
        </edge>
        <edge from="n30" to="n4">
            <attr name="label">
                <string>2</string>
            </attr>
        </edge>
        <edge from="n28" to="n31">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n31" to="n8">
            <attr name="label">
                <string>assignee</string>
            </attr>
        </edge>
        <edge from="n31" to="n6">
            <attr name="label">
                <string>1</string>
            </attr>
        </edge>
        <edge from="n28" to="n32">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n32" to="n12">
            <attr name="label">
                <string>assignee</string>
            </attr>
        </edge>
        <edge from="n32" to="n6">
            <attr name="label">
                <string>1</string>
            </attr>
        </edge>
        <edge from="n32" to="n10">
            <attr name="label">
                <string>2</string>
            </attr>
        </edge>
        <edge from="n28" to="n33">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n33" to="n24">
            <attr name="label">
                <string>assignee</string>
            </attr>
        </edge>
        <edge from="n33" to="n6">
            <attr name="label">
                <string>1</string>
            </attr>
        </edge>
        <edge from="n33" to="n10">
            <attr name="label">
                <string>2</string>
            </attr>
        </edge>
        <edge from="n28" to="n34">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n34" to="n22">
            <attr name="label">
                <string>assignee</string>
            </attr>
        </edge>
        <edge from="n34" to="n2">
            <attr name="label">
                <string>1</string>
            </attr>
        </edge>
        <edge from="n34" to="n2">
            <attr name="label">
                <string>2</string>
            </attr>
        </edge>
        <edge from="n28" to="n35">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n35" to="n20">
            <attr name="label">
                <string>assignee</string>
            </attr>
        </edge>
        <edge from="n35" to="n2">
            <attr name="label">
                <string>1</string>
            </attr>
        </edge>
        <edge from="n35" to="n2">
            <attr name="label">
                <string>2</string>
            </attr>
        </edge>
        <edge from="n28" to="n36">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n36" to="n16">
            <attr name="label">
                <string>assignee</string>
            </attr>
        </edge>
        <edge from="n36" to="n2">
            <attr name="label">
                <string>1</string>
            </attr>
        </edge>
        <edge from="n36" to="n2">
            <attr name="label">
                <string>2</string>
            </attr>
        </edge>
        <edge from="n28" to="n37">
            <attr name="label">
                <string>exp</string>
            </attr>
        </edge>
        <edge from="n37" to="n18">
            <attr name="label">
                <string>assignee</string>
            </attr>
        </edge>
        <edge from="n37" to="n2">
            <attr name="label">
                <string>1</string>
            </attr>
        </edge>
        <edge from="n37" to="n2">
            <attr name="label">
                <string>2</string>
            </attr>
        </edge>
        <edge from="n39" to="n0">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n39" to="n28">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
        <edge from="n40" to="n28">
            <attr name="label">
                <string>source</string>
            </attr>
        </edge>
        <edge from="n40" to="n38">
            <attr name="label">
                <string>target</string>
            </attr>
        </edge>
    </graph>
</gxl>
