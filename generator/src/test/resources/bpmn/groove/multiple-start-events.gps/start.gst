<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="multiple-start-events_start" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>62 87 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:ProcessSnapshot</string>
            </attr>
        </edge>
        <node id="n1">
            <attr name="layout">
                <string>402 152 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>string:"p1"</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>402 72 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:Running</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>417 232 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:Token</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>602 232 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>string:"start1_p1 -&gt; p_p1 (Flow_1p3gdcm)"</string>
            </attr>
        </edge>
        <node id="n5">
            <attr name="layout">
                <string>417 312 0 0</string>
            </attr>
        </node>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:Token</string>
            </attr>
        </edge>
        <node id="n6">
            <attr name="layout">
                <string>602 312 0 0</string>
            </attr>
        </node>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>string:"start2_p1 -&gt; p_p1 (Flow_1bwtd34)"</string>
            </attr>
        </edge>
        <node id="n7">
            <attr name="layout">
                <string>62 407 0 0</string>
            </attr>
        </node>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>type:ProcessSnapshot</string>
            </attr>
        </edge>
        <node id="n8">
            <attr name="layout">
                <string>402 392 0 0</string>
            </attr>
        </node>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>string:"p2"</string>
            </attr>
        </edge>
        <node id="n9">
            <attr name="layout">
                <string>402 632 0 0</string>
            </attr>
        </node>
        <edge from="n9" to="n9">
            <attr name="label">
                <string>type:Running</string>
            </attr>
        </edge>
        <node id="n10">
            <attr name="layout">
                <string>417 552 0 0</string>
            </attr>
        </node>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>type:Token</string>
            </attr>
        </edge>
        <node id="n11">
            <attr name="layout">
                <string>602 552 0 0</string>
            </attr>
        </node>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>string:"start1_p2 -&gt; p_p2 (Flow_120g9dm)"</string>
            </attr>
        </edge>
        <node id="n12">
            <attr name="layout">
                <string>417 472 0 0</string>
            </attr>
        </node>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>type:Token</string>
            </attr>
        </edge>
        <node id="n13">
            <attr name="layout">
                <string>602 472 0 0</string>
            </attr>
        </node>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>string:"start2_p2 -&gt; p_p2 (Flow_1f9dgmw)"</string>
            </attr>
        </edge>
        <edge from="n0" to="n1">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <edge from="n0" to="n2">
            <attr name="label">
                <string>state</string>
            </attr>
        </edge>
        <edge from="n3" to="n4">
            <attr name="label">
                <string>position</string>
            </attr>
        </edge>
        <edge from="n0" to="n3">
            <attr name="label">
                <string>tokens</string>
            </attr>
        </edge>
        <edge from="n5" to="n6">
            <attr name="label">
                <string>position</string>
            </attr>
        </edge>
        <edge from="n0" to="n5">
            <attr name="label">
                <string>tokens</string>
            </attr>
        </edge>
        <edge from="n7" to="n8">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <edge from="n7" to="n9">
            <attr name="label">
                <string>state</string>
            </attr>
        </edge>
        <edge from="n10" to="n11">
            <attr name="label">
                <string>position</string>
            </attr>
        </edge>
        <edge from="n7" to="n10">
            <attr name="label">
                <string>tokens</string>
            </attr>
        </edge>
        <edge from="n12" to="n13">
            <attr name="label">
                <string>position</string>
            </attr>
        </edge>
        <edge from="n7" to="n12">
            <attr name="label">
                <string>tokens</string>
            </attr>
        </edge>
    </graph>
</gxl>
