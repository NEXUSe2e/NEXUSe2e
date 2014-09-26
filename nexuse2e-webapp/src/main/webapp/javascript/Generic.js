/*
 *  NEXUSe2e Business Messaging Open Source
 *  Copyright 2000-2009, Tamgroup and X-ioma GmbH
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation version 2.1 of
 *  the License.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/*
 * Updates the content of the docpane by callback of the tree select event.
 */
function update(url) {
    var docPane = dijit.byId("docpane");
    if (!url) {
        docPane.setContent("Unknown document \"" + url + "\"");
    } else {
        setContentUrl(url);
    }
}

/*
 * Returns the HTML that display the navigation crumbs based on the currently
 * selected tree node.
 */
function getCrumbs() {
    var selectedNode = getSelectedTreeNode();
    if (selectedNode) {
        var crumbString = getCrumbLink(selectedNode);
        while (selectedNode.getParent() && selectedNode.getParent().label
                && selectedNode.getParent().label.length > 0) {
            selectedNode = selectedNode.getParent();
            crumbString = getCrumbLink(selectedNode) + ' &gt ' + crumbString;
        }
        return crumbString;
    } else {
        return "";
    }
    return crumbString;
}

/*
 * Renders a crumb link for a specified node.
 */
function getCrumbLink(selectedNode) {
    return "<a href=\"javascript: expandTreePath('" + selectedNode.id
            + "'); focusNode('" + selectedNode.id + "'); update( '"
            + selectedNode.item.widgetId + "' );\">" + selectedNode.label
            + '</a>';
}

/*
 * Recursively expands the tree to a specific node.
 */
function expandTreePath(selectedNode) {
    if (!selectedNode.getParent) {
        selectedNode = dijit.byId(selectedNode);
    }
    if (selectedNode && selectedNode.getParent()) {
        expandTreePath(selectedNode.getParent());
    }
    if (!selectedNode.isExpanded) {
        getTree()._expandNode(selectedNode);
    }
}

/*
 * Focus a node specified by it's id.
 */
function focusNode(id) {
    var node = dijit.byId(id);
    if (node) {
        getTree().focusNode(node);
    }
}

/*
 * Returns the widget the displays the content.
 */
function getDocPane() {
    return dijit.byId('docpane');
}

/*
 * Returns the widget the displays the menu tree.
 */
function getNavPane() {
    return dojo.byId('navigator');
}

/*
 * Returns the currently selected node.
 */
function getSelectedTreeNode() {
    return getTree().lastFocused;
}

/*
 * Returns the tree widget.
 */
function getTree() {
    return dijit.byId('menuTree');
}

/*
 * Returns the tree's model.
 */
function getTreeModel() {
    return getTree().model;
}

/*
 * Returns the progress bar.
 */
function getProgressBar() {
    return dijit.byId('progressBar');
}

/*
 * Show the progress bar dialog.
 */
function showInProgress() {
    dojo.query("html *").style("cursor", "wait");
    dojo.style("downloadProgress", "visibility", "visible");
}

/*
 * Hide the progress bar dialog.
 */
function hideInProgress() {
    dojo.style("downloadProgress", "visibility", "hidden");
    dojo.query("html *").style("cursor", "");
}

/*
 * Displays an error.
 */
function displayError(message) {
    alert(message);
    hideInProgress();
}

/*
 * Check whether the configuration was changed.
 */
function checkForChangedConfiguration(changed) {
    if (changed) {
        document.getElementById('applyConfiguration').parentNode.className = "active";
        document.getElementById('applyConfiguration').href = "javascript: update('ApplyConfiguration.do')";
        document.getElementById('revertConfiguration').parentNode.className = "active";
        document.getElementById('revertConfiguration').href = "javascript: update('RevertConfiguration.do')";
    } else {
        document.getElementById('applyConfiguration').parentNode.className = "inactive";
        document.getElementById('applyConfiguration').href = "#";
        document.getElementById('revertConfiguration').parentNode.className = "inactive";
        document.getElementById('revertConfiguration').href = "#";
    }
}

/*
 * Refreshes all dynamic nodes in the tree.
 */
function refreshMenuTree() {
    if (getTree() && getTree().rootNode) {
        // the tree's root is virtual, so we take it's first child as our root
        var rootNode = getTree().rootNode.getChildren()[0];
        if (rootNode && rootNode.isExpanded && rootNode.getChildren()) {
            reloadChildren(rootNode.getChildren());
        }
    }
}

/*
 * Called by refreshTree(). Iterates over an array of nodes and finds nodes with
 * dynamic children that have to be reloaded.
 */
function reloadChildren(nodes) {
    if (nodes && nodes[0]) { // not null not empty
        for (var i = 0; i < nodes.length; i++) {
            // load children 'cause "d" stands for "reload me"
            if (nodes[i].item.type == "folder" && nodes[i].item.objectId.substring(0, 1) == "d") {
                updateChildren(nodes[i]);
            }

            // check whether child nodes need refresh
            reloadChildren(nodes[i].getChildren());
        }
    }
}

/*
 * Transitively called by refreshTree(). Decides which nodes must be reloaded or
 * removed and reloads or removes them.
 */
function updateChildren(node) {
    // console.log("Update children of: " + node.item.widgetId + " (" +
    // node.item.objectId + ")");
    getTreeModel().getChildren(node.item, function(children) {
        getTree()._onItemChildrenChange(node.item, children);
    });
}

/*
 * Loads content into the document pane. You may want to use the "update(url)"
 * method instead of calling this directly.
 * 
 * dummy parameter added to avoid ie get method caching
 */
function setContentUrl(contentUrl) {
    var separator = "?";
    if (contentUrl.indexOf('?') > -1) {
        separator = "&";
    }
    contentUrl = contentUrl + separator + "dummy=" + Math.random();
    // alert( 'Form: ' );
    showInProgress();

    // debug(form);
    // alert( 'Action: ' + form.action );
    dojo.xhrGet({
        url : contentUrl,
        handleAs : "text",
        load : function(data) {
            // console.log(data);
            getDocPane().attr("content", data);
            hideInProgress();
        },
        error : function(t, e) {
            displayError(e.message);
        }
    });
}

/*
 * This method submits a form and displays the result in the document pane.
 */
function submitForm(form) {
    // alert( 'Form: ' );
    showInProgress();

    // debug(form);
    // alert( 'Action: ' + form.action );
    dojo.xhrPost({
        handleAs : "text",
        form : form,
        load : function(data) {
            // console.log(data);
            getDocPane().attr("content", data);
            hideInProgress();
        },
        error : function(t, e) {
            displayError(e.message);
        }
    });
}

/*
 * Special submit method for forms that contain a file to upload.
 */
function submitFileForm(form) {
    submitFileFormData(form);
}

/*
 * Called by submitFileForm(form).
 */
function submitFileFormData(form) {
    showInProgress();

    dojo.io.iframe.send({
        form : form,
        content : {
            // indicate in the request that this is a file upload
            "X-Ne2e-File-Upload" : "true"
        },
        load : function(response, ioArgs) {
            // displayError("response = " + JSON.stringify(response) + ", ioArgs
            // = " + JSON.stringify(ioArgs));
            getDocPane().attr("content", response);
            hideInProgress();
            return response;
        },
        error : function(response, ioArgs) {
            displayError("response = " + JSON.stringify(response)
                    + ", ioArgs = " + JSON.stringify(ioArgs));
            hideInProgress();
            return response;
        }
    });
}

//
// "Compress header bar" functionality
//
dojo.require("dojox.fx._base");
function hide() {
    if (dojo.style("logo_div", "height") != 0) {
        var contentheight = dojo.style("navigator", "height") + 164;
        var anim1 = dojo.animateProperty({node: "logo_div", properties: { height: { end: 0 } } });
        var anim2 = dojo.animateProperty({node: "navigator", properties: { top: { end: 32 } } });
        var anim3 = dojo.animateProperty({node: "docpane", properties: { top: { end: 32 } } });
        var anim4 = dojo.animateProperty({node: "machine_div_1", properties: { opacity: { end: 0 } } });
        var anim5 = dojo.animateProperty({node: "machine_div_2", properties: { opacity: { end: 0 } } });
        var anim6 = dojo.animateProperty({node: "nav-global", properties: { top: { end: 0 } } });
        var anim7 = dojo.animateProperty({node: "header", properties: { height: { end: 32 } } });
        var anim8 = dojo.animateProperty({node: "navigator", properties: { height: { end: contentheight } } });
        var anim9 = dojo.animateProperty({node: "machine_div", properties: { height: { end: 0 },right:{end: 100 },top:{end: 0 } } });
        var anim10 = dojo.animateProperty({node: "machine_div2", properties: { height: { end: 0 },right:{end: 100 }, top:{end: 0 } } });
        var anim11 = dojo.animateProperty({node: "machine_div3", properties: { height: { end: 0 },right:{end: 100 }, top:{end: 0 } } });
        dojo.byId("hide_banner").setAttribute("src", "images/icons/bullet_arrow_down.png");
        dojo.fx.combine([anim1, anim2, anim3, anim4, anim5, anim6, anim7, anim8, anim9, anim10, anim11]).play();
    } else {
        var contentheight = dojo.style("navigator", "height") - 164;
        var anim1 = dojo.animateProperty({node: "logo_div", properties: { height: { end: 164 } } });
        var anim2 = dojo.animateProperty({node: "navigator", properties: { top: { end: 202 } } });
        var anim3 = dojo.animateProperty({node: "docpane", properties: { top: { end: 202 } } });
        var anim4 = dojo.animateProperty({node: "machine_div_1", properties: { opacity: { end: 100 } } });
        var anim5 = dojo.animateProperty({node: "machine_div_2", properties: { opacity: { end: 100 } } });
        var anim6 = dojo.animateProperty({node: "nav-global", properties: { top: { end: 164 } } });
        var anim7 = dojo.animateProperty({node: "header", properties: { height: { end: 202 } } });
        var anim8 = dojo.animateProperty({node: "navigator", properties: { height: { end: contentheight } } });
        var anim9 = dojo.animateProperty({node: "machine_div", properties: { height: { end: 0 },right:{end: 20 },top:{end: 20 } } });
        var anim10 = dojo.animateProperty({node: "machine_div2", properties: { height: { end: 28 },right:{end: 20 }, top:{end: 0 } } });
        var anim11 = dojo.animateProperty({node: "machine_div3", properties: { height: { end: 13 },right:{end: 20 }, top:{end: 0 } } });
        dojo.byId("hide_banner").setAttribute("src", "images/icons/bullet_arrow_up.png");
        dojo.fx.combine([anim1, anim2, anim3, anim4, anim5, anim6, anim7, anim8, anim9, anim10, anim11]).play();
    }
}






//
// Utility functions
//

function checkKey(e) {
    if (!e) {
        e = window.event;
    }
    if (e.which) {
        code = e.which;
    } else if (e.keyCode) {
        code = e.keyCode;
    }
    return (code != 13);
}

function confirmDelete(deleteMsg) {
    return confirm(deleteMsg);
}

function embeddedSpaces(inputField) {
    while ('' + inputField.value.charAt(0) == ' ')
        inputField.value = inputField.value.substring(1,
                inputField.value.length);
    while ('' + inputField.value.charAt(inputField.value.length - 1) == ' ')
        inputField.value = inputField.value.substring(0,
                inputField.value.length - 1);
    return (inputField.value.indexOf(' ') != -1);
}