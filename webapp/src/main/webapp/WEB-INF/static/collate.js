YUI().use("io", "json", "dump", "event", "node", "escape", "array-extras", function(Y) {
    var create = Y.Node.create,
        sub = Y.Lang.sub,
        svgContainer = null,
        tableContainer = null,
        graphVizDotContainer = null,
        graphmlContainer = null,
        teiPsContainer = null;

    function addWitness(e) {
        if (e) e.preventDefault();
        var witnesses = getWitnesses();
        witnesses.push("");
        setWitnesses(witnesses);
    }

    function getWitnesses() {
        var contents = []
        Y.all("#witnesses textarea").each(function(w) {
            var content = w.get("value").replace(/^\s*/, "").replace(/\s*$/, "");
            if (content.length > 0) contents.push(content);
        });
        return contents;
    }

    function setWitnesses(contents) {
        while (contents.length < 2) {
            contents.push("");
        }

        var witnessContainer = Y.one("#witnesses");
        witnessContainer.setContent("");

        for (var wc = 0; wc < contents.length; wc++) {
            var witnessData = { id: "witness-" + wc.toString(), label: "Witness #" + (wc + 1).toString(), contents: Y.Escape.html(contents[wc]) };
            witnessContainer.append(create('<div class="yui3-g form-element" />')
                    .append(create('<div class="yui3-u-1-6 form-label"/>').append(sub('<label for="{id}">{label}:</label>', witnessData)))
                    .append(create('<div class="yui3-u form-input"/>').append(sub('<textarea id="{id}" name="{id}" rows="3" cols="80" style="width: 20em">{contents}</textarea>', witnessData))));
        }

        Y.some(contents, function(c, i) {
            if (c.length == 0) {
                Y.one("#witness-" + i.toString()).focus();
                return true;
            }
            return false;
        });

        Y.on("focus", function(e) { this.select(); }, "#witnesses textarea");
        return contents;
    }

    function collate(e) {
        if (e) e.preventDefault();

        clearResults();

        var witnesses = getWitnesses();
        if (witnesses.length <= 1) {
            return;
        }

        // build the collation input
        var collation = { witnesses:[] };
        Y.each(witnesses, function (w, i) {
            collation.witnesses.push({ id:"W" + (i + 1).toString(), content:witnesses[i] });
        });

        var collationData = Y.JSON.stringify(collation);
        var callCollator = function(resultType, callback) {
            Y.io(cp + "/", {
                method:"post",
                headers:{
                    "Content-Type":"application/json",
                    "Accept": resultType
                },
                data: collationData,
                on:{
                    success: callback,
                    failure: function (transactionId, resp) { alert(Y.dump(resp)); }
                }
            });
        };

        callCollator("image/svg+xml", function (transactionId, resp) {
            svgContainer.getDOMNode().appendChild(document.importNode(resp.responseXML.documentElement, true));
            tableContainer.scrollIntoView();
        });
        callCollator("application/json", function (transactionId, resp) {
            var at = Y.JSON.parse(resp.responseText);
            var table = create('<table class="alignment"/>');
            tableContainer.append(table);
            var cells = []
            var variantStatus = [];
            Y.each(at.table, function (r) {
                var cellContents = [];
                Y.each(r, function (c) {
                    cellContents.push(c == null ? null : Y.Array.reduce(c, "", function (str, next) {
                        return str + (str.length == 0 ? "" : " ") + next;
                    }));
                });
                cells.push(cellContents);
                variantStatus.push(Y.Array.dedupe(Y.Array.filter(cellContents, function (c) {
                    return (c != null);
                })).length == 1);
            });
            for (var wc = 0; wc < at.sigils.length; wc++) {
                var column = create("<tr/>");
                column.append('<th>' + Y.Escape.html(at.sigils[wc]) + '</th>');
                Y.each(cells, function (r, cc) {
                    var c = r[wc];
                    column.append('<td class="' + (variantStatus[cc] ? "invariant" : "variant") + (c == null ? " gap" : "") + '">' + (c == null ? "" : Y.Escape.html(c)));
                });
                table.append(column);
            }
        });
        callCollator("text/plain", function (transactionId, resp) {
            var textArea = create('<textarea rows="10" style="width: 20em" readonly="readonly">' + Y.Escape.html(resp.responseText) + '</textarea>');
            graphVizDotContainer.append(textArea);
        });
        callCollator("application/graphml+xml", function (transactionId, resp) {
            var textArea = create('<textarea rows="10" style="width: 20em" readonly="readonly">' + Y.Escape.html(resp.responseText) + '</textarea>');
            graphmlContainer.append(textArea);
        });

        callCollator("application/tei+xml", function (transactionId, resp) {
            var textArea = create('<textarea rows="10" style="width: 20em" readonly="readonly">' + Y.Escape.html(resp.responseText) + '</textarea>');
            teiPsContainer.append(textArea);
        });
    }

    function selectExample(e) {
        clearResults();
        var selected = this.get("value").replace(/^e/, "");
        if (selected.length == 0) {
            setWitnesses(["", ""]);
        } else {
            setWitnesses(examples[parseInt(selected)]);
            collate();
        }
    }

    function clearResults() {
        svgContainer.empty();
        tableContainer.empty();
        graphVizDotContainer.empty();
        graphmlContainer.empty();
        teiPsContainer.empty();
    }

    Y.on("domready", function() {
        svgContainer = Y.one("#variant-graph-svg");
        tableContainer = Y.one("#alignment-table");
        graphVizDotContainer = Y.one("#graphviz-dot");
        graphmlContainer = Y.one("#graphml");
        teiPsContainer = Y.one("#tei-ps");

        setWitnesses(["", ""]);

        var exampleSelect = Y.one("#examples");
        Y.each(examples, function(e, i) {
            var title = e[0];
            if (title.length > 80) title = title.substring(0, 80) + "…";
            var exampleData = { value: "e" + i.toString(), title : title };
            exampleSelect.append(sub('<option value="{value}">{title}</option>', exampleData));
        });

        Y.on("change", selectExample, "#examples");
        Y.on("click", addWitness, "#add-witness");
        Y.on("submit", collate, "#collate-form");
    });
});